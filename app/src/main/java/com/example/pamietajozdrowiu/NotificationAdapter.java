package com.example.pamietajozdrowiu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
                // Ustaw kolor przycisku (np. domyślny lub fioletowy)
//                holder.takeMedicationButton.setBackgroundColor(ContextCompat.getColor(context, R.color.gray));
            } else {
                // Czas powiadomienia jeszcze nie minął - przycisk jest nieaktywny lub ukryty
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
            // Aktualizuj status powiadomienia w bazie danych
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE NOTIFICATION_SCHEDULE SET IS_TAKEN = 1 WHERE ID_NOTIFICATION_SCHEDULE = ?", new Object[]{notification.getId()});

            // Zmniejsz ilość tabletek w DRUGS
            db.execSQL("UPDATE DRUGS SET PILLS_QUANTITY = PILLS_QUANTITY - 1 WHERE ID_DRUG = ?", new Object[]{notification.getDrugId()});

            // Aktualizuj obiekt Notification
            notification.setTaken(true);
            notifyItemChanged(position);

            // Wyświetl Toast
            Toast.makeText(context, "Zapisano przyjęcie leku", Toast.LENGTH_SHORT).show();
        });
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
}
