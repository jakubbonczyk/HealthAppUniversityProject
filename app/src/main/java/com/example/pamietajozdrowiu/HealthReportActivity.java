package com.example.pamietajozdrowiu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HealthReportActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private TextView reportTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_report);

        dbHelper = new DatabaseHelper(this);
        reportTextView = findViewById(R.id.reportTextView);

        generateHealthReport();
    }

    private void generateHealthReport() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TYPE, AVG(VALUE) as avgValue, COUNT(*) as count FROM HEALTH_RESULTS GROUP BY TYPE", null);

        StringBuilder report = new StringBuilder();
        report.append("Raport zdrowotny:\n\n");

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
            String avgValue = cursor.getString(cursor.getColumnIndexOrThrow("avgValue"));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));

            report.append("Typ badania: ").append(type).append("\n");
            report.append("Średnia wartość: ").append(avgValue).append("\n");
            report.append("Liczba wpisów: ").append(count).append("\n\n");
        }

        cursor.close();
        reportTextView.setText(report.toString());
    }
}
