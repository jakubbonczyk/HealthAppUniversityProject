package com.example.pamietajozdrowiu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Pobranie czasu przypomnienia
        String reminderTime = intent.getStringExtra("reminder_time");

        // Sprawdzenie, czy reminderTime jest null
        if (reminderTime == null || reminderTime.isEmpty()) {
            Log.e("NotificationReceiver", "Brak wymaganych danych w Intent (reminder_time)");
            return;
        }

        // Wyświetlenie powiadomienia
        String notificationContent = "Przypomnienie o przyjęciu leku o godzinie " + reminderTime;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "medication_reminders")
                .setSmallIcon(R.drawable.smile)
                .setContentTitle("Przypomnienie o leku")
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d("NotificationReceiver", "Powiadomienie zostało wyświetlone: " + notificationContent);
        } else {
            Log.e("NotificationReceiver", "Brak uprawnień do wyświetlania powiadomień.");
        }
    }

}
