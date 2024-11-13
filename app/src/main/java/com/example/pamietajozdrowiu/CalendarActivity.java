package com.example.pamietajozdrowiu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private CalendarView calendarView;
    private RecyclerView scheduledDrugsRecyclerView;
    private DrugAdapter drugAdapter;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<Drug> scheduledDrugs;

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

        // Listener na CalendarView
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            Log.d(TAG, "Selected date: " + selectedDate);
            loadDrugsForSelectedDay(selectedDate);
        });
    }

    // Metoda ładowania leków na wybrany dzień
    private void loadDrugsForSelectedDay(String date) {
        scheduledDrugs.clear();

        int dayOfWeekInt = getDayIntFromDate(date); // Pobranie dnia tygodnia jako liczby
        if (dayOfWeekInt == -1) {
            Toast.makeText(this, "Błąd przy pobieraniu dnia tygodnia.", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "SELECT d.ID_DRUG, d.NAME, d.PILLS_QUANTITY, d.EXPIRATION_DATE, d.IMAGE " +
                "FROM DRUGS d " +
                "INNER JOIN NOTIFICATION_SCHEDULE ns ON d.ID_DRUG = ns.ID_DRUG " +
                "WHERE ns.FREQUENCY = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(dayOfWeekInt)})) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID_DRUG"));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                    int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                    String expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPIRATION_DATE"));
                    byte[] imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));

                    Drug drug = new Drug(id, name, pillsQuantity, expirationDate, imageBlob);
                    scheduledDrugs.add(drug);
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "Brak leków zaplanowanych na ten dzień.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Błąd ładowania danych: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        drugAdapter.notifyDataSetChanged();
    }

    // Metoda pomocnicza do pobrania dnia tygodnia dla daty
    private int getDayIntFromDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = format.parse(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            // Pobierz dzień tygodnia i dostosuj do naszego formatu
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    return 7; // Niedziela jako 7
                case Calendar.MONDAY:
                    return 1; // Poniedziałek jako 1
                case Calendar.TUESDAY:
                    return 2;
                case Calendar.WEDNESDAY:
                    return 3;
                case Calendar.THURSDAY:
                    return 4;
                case Calendar.FRIDAY:
                    return 5;
                case Calendar.SATURDAY:
                    return 6; // Sobota jako 6
                default:
                    return -1; // W przypadku błędu
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
