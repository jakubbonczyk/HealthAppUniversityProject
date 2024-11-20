package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private CalendarView calendarView;
    private RecyclerView scheduledDrugsRecyclerView;
    private DrugAdapter drugAdapter;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<Drug> scheduledDrugs;

    // Mapa dat do list leków
    private Map<String, List<Drug>> dateDrugMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        // Inicjalizacja bazy danych
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // Inicjalizacja widoków
        calendarView = findViewById(R.id.calendarView);
        scheduledDrugsRecyclerView = findViewById(R.id.scheduledDrugsRecyclerView);

        // Ustawienie RecyclerView
        scheduledDrugsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduledDrugs = new ArrayList<>();
        drugAdapter = new DrugAdapter(scheduledDrugs, this);
        scheduledDrugsRecyclerView.setAdapter(drugAdapter);

        Button backButton = findViewById(R.id.button4);
        Button editScheduleButton = findViewById(R.id.editScheduleButton);

        // Inicjalizacja mapy dat do leków
        dateDrugMap = new HashMap<>();

        // Wczytanie danych i wygenerowanie mapy
        generateDateDrugMap();

        // Listener na CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            Log.d(TAG, "Selected date: " + selectedDate);
            loadDrugsForSelectedDay(selectedDate);
        });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, MainActivity.class);
            startActivity(intent);
        });

        editScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, YourDrugsActivity.class);
            startActivity(intent);
        });
    }

    // Metoda generująca mapę dat do leków
    private void generateDateDrugMap() {
        dateDrugMap.clear();

        // Pobranie wszystkich leków i ich harmonogramów
        String query = "SELECT d.ID_DRUG, d.NAME, d.PILLS_QUANTITY, d.EXPIRATION_DATE, d.IMAGE, ns.FREQUENCY, ns.REMINDER_TIME " +
                "FROM DRUGS d " +
                "INNER JOIN NOTIFICATION_SCHEDULE ns ON d.ID_DRUG = ns.ID_DRUG";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    int drugId = cursor.getInt(cursor.getColumnIndexOrThrow("ID_DRUG"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                    int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                    String expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPIRATION_DATE"));
                    byte[] imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));
                    int frequency = cursor.getInt(cursor.getColumnIndexOrThrow("FREQUENCY"));

                    Drug drug = new Drug(drugId, name, pillsQuantity, expirationDate, imageBlob);

                    // Generowanie przyszłych dat przyjmowania leku
                    generateDrugSchedule(drug, frequency, pillsQuantity);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Toast.makeText(this, "Błąd ładowania danych: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Metoda generująca przyszłe daty przyjmowania leku
    private void generateDrugSchedule(Drug drug, int frequency, int pillsQuantity) {
        Calendar calendar = Calendar.getInstance();
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        int daysUntilNextDose = (frequency - currentDayOfWeek + 7) % 7;
        // Jeśli dzisiaj jest dniem zaplanowanym, daysUntilNextDose będzie 0
        // Nie zmieniamy go, aby uwzględnić dzisiejszą datę
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilNextDose);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        int dosesScheduled = 0;

        while (dosesScheduled < pillsQuantity) {
            String dateKey = sdf.format(calendar.getTime());

            // Dodanie leku do mapy dla danej daty
            if (!dateDrugMap.containsKey(dateKey)) {
                dateDrugMap.put(dateKey, new ArrayList<>());
            }
            dateDrugMap.get(dateKey).add(drug);

            // Przygotowanie do następnej dawki
            calendar.add(Calendar.DAY_OF_YEAR, 7); // Ponieważ częstotliwość to co tydzień
            dosesScheduled++;
        }
    }

    // Metoda ładowania leków na wybrany dzień
    private void loadDrugsForSelectedDay(String date) {
        scheduledDrugs.clear();

        if (dateDrugMap.containsKey(date)) {
            scheduledDrugs.addAll(dateDrugMap.get(date));
        } else {
            Toast.makeText(this, "Brak leków zaplanowanych na ten dzień.", Toast.LENGTH_SHORT).show();
        }

        drugAdapter.notifyDataSetChanged();
    }
}
