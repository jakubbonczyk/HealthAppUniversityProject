package com.example.pamietajozdrowiu;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class YourFeelingsActivity extends AppCompatActivity {

    private RecyclerView todaysMedicationsRecyclerView;
    private MedicationIntakeAdapter medicationIntakeAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Drug> drugList;
    private String selectedFeeling = ""; // Przechowuje aktualnie wybrane uczucie

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.your_feelings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        Button button = findViewById(R.id.button4);
        Button sadButton = findViewById(R.id.button5);
        Button neutralButton = findViewById(R.id.button6);
        Button happyButton = findViewById(R.id.button7);
        Button saveFeelingsButton = findViewById(R.id.saveFeelingsButton);
        Button showHistoryButton = findViewById(R.id.showHistoryButton);
        EditText notesEditText = findViewById(R.id.notesEditText);
        EditText feelingText = findViewById(R.id.feelingtext);
        TextView currentDateTextView = findViewById(R.id.currentDateTextView);

        // Ustawienie aktualnej daty
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        currentDateTextView.setText("Dzisiaj jest: " + currentDate);

        // Pole `feelingtext` jest wyłącznie do odczytu
        feelingText.setEnabled(false);

        // Sprawdzenie, czy uczucia zostały już zapisane dla dzisiejszego dnia
        boolean feelingsAlreadySaved = checkIfFeelingsAlreadySaved(currentDate);

        // Wyłączanie przycisku zapisu, jeśli uczucia już zapisano
        if (feelingsAlreadySaved) {
            saveFeelingsButton.setEnabled(false);
            Toast.makeText(this, "Uczucia na dzisiaj już zapisano!", Toast.LENGTH_SHORT).show();
        }

        // RecyclerView dla leków
        todaysMedicationsRecyclerView = findViewById(R.id.todaysMedicationsRecyclerView);
        todaysMedicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadTodaysMedications();

        sadButton.setOnClickListener(v -> updateFeeling("Czujesz się źle!", feelingText));
        neutralButton.setOnClickListener(v -> updateFeeling("Czujesz się neutralnie!", feelingText));
        happyButton.setOnClickListener(v -> updateFeeling("Czujesz się świetnie!", feelingText));

        button.setOnClickListener(v -> {
            Intent intent = new Intent(YourFeelingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        saveFeelingsButton.setOnClickListener(v -> saveFeeling(currentDate, notesEditText));

        showHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(YourFeelingsActivity.this, FeelingsHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void updateFeeling(String feeling, EditText feelingText) {
        selectedFeeling = feeling;
        feelingText.setText(feeling);
        Toast.makeText(this, feeling, Toast.LENGTH_SHORT).show();
    }

    private boolean checkIfFeelingsAlreadySaved(String currentDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM USER_FEELINGS WHERE DATE = ?";
        Cursor cursor = db.rawQuery(query, new String[]{currentDate});

        boolean exists = false;
        if (cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0; // Jeśli istnieje przynajmniej jeden rekord z dzisiejszą datą
        }
        cursor.close();
        return exists;
    }

    private void saveFeeling(String currentDate, EditText notesEditText) {
        if (selectedFeeling.isEmpty()) {
            Toast.makeText(this, "Najpierw wybierz, jak się czujesz!", Toast.LENGTH_SHORT).show();
            return;
        }

        String notes = notesEditText.getText().toString().trim();
        int userId = 1; // Pobierz ID użytkownika w zależności od logiki aplikacji

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("NAME", selectedFeeling);
        values.put("VALUE", getFeelingValue(selectedFeeling));
        values.put("DATE", currentDate);
        values.put("NOTES", notes);
        values.put("ID_USER", userId);

        long result = db.insert("USER_FEELINGS", null, values);
        if (result != -1) {
            Toast.makeText(this, "Zapisano Twoje uczucia i notatki!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wystąpił błąd podczas zapisu.", Toast.LENGTH_SHORT).show();
        }
    }

    private int getFeelingValue(String feeling) {
        switch (feeling) {
            case "Czujesz się źle!":
                return 1;
            case "Czujesz się neutralnie!":
                return 2;
            case "Czujesz się świetnie!":
                return 3;
            default:
                return 0;
        }
    }

    private void loadTodaysMedications() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        String query = "SELECT DISTINCT d.* FROM DRUGS d " +
                "INNER JOIN NOTIFICATION_SCHEDULE ns ON d.ID_DRUG = ns.ID_DRUG " +
                "WHERE ns.FREQUENCY = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(today)});

        drugList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID_DRUG"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                String expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPIRATION_DATE"));
                byte[] imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));

                Drug drug = new Drug(id, name, pillsQuantity, expirationDate, imageBlob);
                drugList.add(drug);
            } while (cursor.moveToNext());
        }
        cursor.close();

        medicationIntakeAdapter = new MedicationIntakeAdapter(drugList, this);
        todaysMedicationsRecyclerView.setAdapter(medicationIntakeAdapter);
    }
}
