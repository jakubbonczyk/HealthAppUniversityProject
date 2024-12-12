package com.example.pamietajozdrowiu;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
        holder.snoozeButton.setText("");

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

                holder.snoozeButton.setEnabled(false);
                holder.snoozeButton.setBackgroundColor(Color.GRAY);
            } else if (currentDateTime.after(notificationDateTime)) {
                // Czas powiadomienia minął - przyciski są aktywne
                holder.takeMedicationButton.setEnabled(true);
                holder.snoozeButton.setEnabled(true);
                holder.snoozeButton.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            } else {
                // Czas powiadomienia jeszcze nie minął - przyciski są nieaktywne
                holder.takeMedicationButton.setEnabled(false);
                holder.takeMedicationButton.setBackgroundColor(Color.GRAY);

                holder.snoozeButton.setEnabled(false);
                holder.snoozeButton.setBackgroundColor(Color.GRAY);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // W razie błędu parsowania, ustaw przyciski jako nieaktywne
            holder.takeMedicationButton.setEnabled(false);
            holder.takeMedicationButton.setBackgroundColor(Color.GRAY);

            holder.snoozeButton.setEnabled(false);
            holder.snoozeButton.setBackgroundColor(Color.GRAY);
        }

        holder.takeMedicationButton.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE NOTIFICATION_SCHEDULE SET IS_TAKEN = 1 WHERE ID_NOTIFICATION_SCHEDULE = ?", new Object[]{notification.getId()});

            db.execSQL("UPDATE DRUGS SET PILLS_QUANTITY = PILLS_QUANTITY - 1 WHERE ID_DRUG = ?", new Object[]{notification.getDrugId()});

            notification.setTaken(true);
            notifyItemChanged(position);

            Toast.makeText(context, "Zapisano przyjęcie leku", Toast.LENGTH_SHORT).show();
        });

        holder.snoozeButton.setOnClickListener(v -> {
            showSnoozeDialog(notification);
        });
    }


    private void showSnoozeDialog(Notification notification) {
        // Opcje do wyboru
        String[] snoozeOptions = {"1 minuta", "10 minut", "30 minut", "1 godzina", "2 godziny"};
        int[] snoozeTimes = {1, 10, 30, 60, 120};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Drzemka")
                .setItems(snoozeOptions, (dialog, which) -> {
                    int snoozeMinutes = snoozeTimes[which];
                    snoozeNotification(notification, snoozeMinutes);
                    Toast.makeText(context, "Drzemka na " + snoozeOptions[which] + " została ustawiona", Toast.LENGTH_SHORT).show();
                });
        builder.create().show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void snoozeNotification(Notification notification, int minutes) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);

        // Usuń przekazywanie drug_name, zostaw tylko reminder_time
        intent.putExtra("reminder_time", notification.getReminderTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                notification.getId(), // Użyj unikalnego ID
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long snoozeTime = System.currentTimeMillis() + minutes * 60 * 1000;
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, snoozeTime, pendingIntent);
            Log.d("NotificationAdapter", "Zaplanowano nowe powiadomienie na: " + new Date(snoozeTime));
        }
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        Button takeMedicationButton;
        Button snoozeButton;

        public ViewHolder(View itemView) {
            super(itemView);
            takeMedicationButton = itemView.findViewById(R.id.takeMedicationButton);
            snoozeButton = itemView.findViewById(R.id.snoozeButton);
        }
    }
}
