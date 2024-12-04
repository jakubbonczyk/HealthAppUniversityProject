package com.example.pamietajozdrowiu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private Context context;
    private DatabaseHelper dbHelper;

    public NotificationAdapter(List<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationAdapter.ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        String displayText = notification.getDate() + " - " + notification.getReminderTime();

        holder.takeMedicationButton.setText(displayText);

        // Parsuj datę i czas powiadomienia
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String notificationDateTimeString = notification.getDate() + " " + notification.getReminderTime();

        try {
            Date notificationDateTime = dateTimeFormat.parse(notificationDateTimeString);
            Date currentDateTime = new Date();

            if (notification.isTaken()) {
                // Lek został już przyjęty
                holder.takeMedicationButton.setText("Przyjęto");
                holder.takeMedicationButton.setEnabled(false);
                holder.takeMedicationButton.setBackgroundColor(Color.GRAY);
            } else if (currentDateTime.after(notificationDateTime)) {
                // Czas powiadomienia minął - przycisk jest aktywny
                holder.takeMedicationButton.setEnabled(true);
            } else {
                // Czas powiadomienia jeszcze nie minął - przycisk jest nieaktywny
                holder.takeMedicationButton.setEnabled(false);
                holder.takeMedicationButton.setBackgroundColor(Color.GRAY);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // W razie błędu parsowania, ustaw przycisk jako nieaktywny
            holder.takeMedicationButton.setEnabled(false);
            holder.takeMedicationButton.setBackgroundColor(Color.GRAY);
        }

        holder.takeMedicationButton.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE NOTIFICATION_SCHEDULE SET IS_TAKEN = 1 WHERE ID_NOTIFICATION_SCHEDULE = ?", new Object[]{notification.getId()});

            db.execSQL("UPDATE DRUGS SET PILLS_QUANTITY = PILLS_QUANTITY - 1 WHERE ID_DRUG = ?", new Object[]{notification.getDrugId()});

            Cursor cursor = db.rawQuery("SELECT PILLS_QUANTITY FROM DRUGS WHERE ID_DRUG = ?", new String[]{String.valueOf(notification.getDrugId())});
            if (cursor.moveToFirst()) {
                int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                if (pillsQuantity < 5) {
                    sendLowStockNotification(notification.getDrugId(), pillsQuantity);
                }
            }
            cursor.close();

            // Aktualizuj obiekt Notification
            notification.setTaken(true);
            notifyItemChanged(position);

            // Wyświetl Toast
            Toast.makeText(context, "Zapisano przyjęcie leku", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateScheduledReminders(int drugId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Pobierz aktualną liczbę tabletek
        Cursor cursor = db.rawQuery("SELECT PILLS_QUANTITY FROM DRUGS WHERE ID_DRUG = ?", new String[]{String.valueOf(drugId)});
        int pillsQuantity = 0;
        if (cursor.moveToFirst()) {
            pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
        }
        cursor.close();

        // Pobierz wszystkie przyszłe powiadomienia dla tego leku
        Cursor notificationCursor = db.rawQuery("SELECT ID_NOTIFICATION_SCHEDULE FROM NOTIFICATION_SCHEDULE WHERE ID_DRUG = ? AND IS_TAKEN = 0 ORDER BY ID_NOTIFICATION_SCHEDULE ASC", new String[]{String.valueOf(drugId)});

        int count = 0;
        while (notificationCursor.moveToNext()) {
            int notificationId = notificationCursor.getInt(notificationCursor.getColumnIndexOrThrow("ID_NOTIFICATION_SCHEDULE"));
            if (count >= pillsQuantity) {
                // Usuń nadmiarowe powiadomienie
                db.delete("NOTIFICATION_SCHEDULE", "ID_NOTIFICATION_SCHEDULE = ?", new String[]{String.valueOf(notificationId)});
                // Anuluj powiązany alarm
                cancelAlarm(notificationId);
            }
            count++;
        }
        notificationCursor.close();
    }

    private void cancelAlarm(int notificationId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button takeMedicationButton;

        public ViewHolder(View itemView) {
            super(itemView);
            takeMedicationButton = itemView.findViewById(R.id.takeMedicationButton);
        }
    }

    private void sendLowStockNotification(int drugId, int pillsQuantity) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT NAME FROM DRUGS WHERE ID_DRUG = ?", new String[]{String.valueOf(drugId)});
        String drugName = "lek";
        if (cursor.moveToFirst()) {
            drugName = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
        }
        cursor.close();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medication_reminders")
                .setSmallIcon(R.drawable.face)
                .setContentTitle("Niska liczba leków")
                .setContentText("Liczba tabletek dla leku \"" + drugName + "\" wynosi tylko " + pillsQuantity + ". Czas dokupić nowe opakowanie!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(drugId, builder.build());
        }
    }

}
