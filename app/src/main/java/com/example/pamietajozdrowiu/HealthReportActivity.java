package com.example.pamietajozdrowiu;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HealthReportActivity extends AppCompatActivity {

    private Button startDateButton, endDateButton, progressStartDateButton, progressEndDateButton;
    private TextView reportTextView, progressTextView;

    private String startDate, endDate, progressStartDate, progressEndDate;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

        dbHelper = new DatabaseHelper(this);

        // Przyciski dat
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        progressStartDateButton = findViewById(R.id.progressStartDateButton);
        progressEndDateButton = findViewById(R.id.progressEndDateButton);

        // Teksty raportu
        reportTextView = findViewById(R.id.reportTextView);
        progressTextView = findViewById(R.id.progressTextView);

        // Obsługa wyboru dat
        startDateButton.setOnClickListener(v -> showDatePickerDialog(date -> {
            startDate = date;
            startDateButton.setText("Data początkowa: " + date);
        }));

        endDateButton.setOnClickListener(v -> showDatePickerDialog(date -> {
            endDate = date;
            endDateButton.setText("Data końcowa: " + date);
        }));

        progressStartDateButton.setOnClickListener(v -> showDatePickerDialog(date -> {
            progressStartDate = date;
            progressStartDateButton.setText("Data początkowa: " + date);
        }));

        progressEndDateButton.setOnClickListener(v -> showDatePickerDialog(date -> {
            progressEndDate = date;
            progressEndDateButton.setText("Data końcowa: " + date);
        }));

        // Generowanie raportu zdrowotnego
        Button generateReportButton = findViewById(R.id.generateReportButton);
        generateReportButton.setOnClickListener(v -> generateHealthReport());

        // Wyświetlanie postępów leczenia
        Button checkProgressButton = findViewById(R.id.checkProgressButton);
        checkProgressButton.setOnClickListener(v -> displayProgress());
    }

    private void generateHealthReport() {
        if (startDate == null || endDate == null) {
            reportTextView.setText("Wybierz zakres dat przed wygenerowaniem raportu!");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TYPE, AVG(VALUE) as avgValue, COUNT(*) as count FROM HEALTH_RESULTS WHERE DATE BETWEEN ? AND ? GROUP BY TYPE",
                new String[]{startDate, endDate});

        StringBuilder report = new StringBuilder();
        report.append("Raport zdrowotny dla zakresu dat: ").append(startDate).append(" - ").append(endDate).append("\n\n");

        if (cursor.getCount() == 0) {
            report.append("Brak wyników dla wybranego zakresu dat.");
        } else {
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                String avgValue = cursor.getString(cursor.getColumnIndexOrThrow("avgValue"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));

                report.append("Typ badania: ").append(type).append("\n");
                report.append("Średnia wartość: ").append(avgValue).append("\n");
                report.append("Liczba wpisów: ").append(count).append("\n\n");
            }
        }

        cursor.close();
        reportTextView.setText(report.toString());
    }

    private void displayProgress() {
        if (progressStartDate == null || progressEndDate == null) {
            progressTextView.setText("Wybierz zakres dat dla postępów leczenia!");
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TYPE, AVG(VALUE) as avgValue, COUNT(*) as count FROM HEALTH_RESULTS WHERE DATE BETWEEN ? AND ? GROUP BY TYPE",
                new String[]{progressStartDate, progressEndDate});

        StringBuilder progressReport = new StringBuilder();
        progressReport.append("Raport postępów leczenia dla zakresu dat: ").append(progressStartDate).append(" - ").append(progressEndDate).append("\n\n");

        if (cursor.getCount() == 0) {
            progressReport.append("Brak wyników dla wybranego zakresu dat.");
        } else {
            while (cursor.moveToNext()) {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                String avgValue = cursor.getString(cursor.getColumnIndexOrThrow("avgValue"));
                int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));

                progressReport.append("Typ badania: ").append(type).append("\n");
                progressReport.append("Średnia wartość: ").append(avgValue).append("\n");
                progressReport.append("Liczba wpisów: ").append(count).append("\n\n");
            }
        }

        cursor.close();
        progressTextView.setText(progressReport.toString());
    }

    private void showDatePickerDialog(DatePickerCallback callback) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
            callback.onDateSelected(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    interface DatePickerCallback {
        void onDateSelected(String date);
    }
}
