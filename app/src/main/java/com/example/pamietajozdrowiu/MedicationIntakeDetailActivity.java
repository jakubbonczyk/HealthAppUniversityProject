package com.example.pamietajozdrowiu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MedicationIntakeDetailActivity extends AppCompatActivity {

    private int drugId;
    private String drugName;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_intake_detail);

        drugId = getIntent().getIntExtra("DRUG_ID", -1);
        drugName = getIntent().getStringExtra("DRUG_NAME");

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadNotifications();

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadNotifications() {
        Cursor cursor = db.rawQuery("SELECT * FROM NOTIFICATION_SCHEDULE WHERE ID_DRUG = ?", new String[]{String.valueOf(drugId)});
        List<Notification> notificationList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID_NOTIFICATION_SCHEDULE"));
                String reminderTime = cursor.getString(cursor.getColumnIndexOrThrow("REMINDER_TIME"));
                int isTakenInt = cursor.getInt(cursor.getColumnIndexOrThrow("IS_TAKEN"));
                boolean isTaken = isTakenInt == 1;

                String frequency = cursor.getString(cursor.getColumnIndexOrThrow("FREQUENCY"));
                String date = getNextDateForFrequency(frequency);

                Notification notification = new Notification(id, drugId, date, reminderTime, isTaken);
                notificationList.add(notification);
            } while (cursor.moveToNext());
        }
        cursor.close();

        notificationAdapter = new NotificationAdapter(notificationList, this);
        notificationsRecyclerView.setAdapter(notificationAdapter);
    }

    private String getNextDateForFrequency(String frequency) {
        // Implementacja funkcji zwracającej najbliższą datę dla danego dnia tygodnia
        // Na potrzeby przykładu zwróćmy dzisiejszą datę
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

}
