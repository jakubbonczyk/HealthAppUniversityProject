package com.example.pamietajozdrowiu;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DrugDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DRUG_ID = "com.example.pamietajozdrowiu.EXTRA_DRUG_ID";
    public static final String EXTRA_DRUG_NAME = "com.example.pamietajozdrowiu.EXTRA_DRUG_NAME";
    public static final String EXTRA_DRUG_PILLS_QUANTITY = "com.example.pamietajozdrowiu.EXTRA_DRUG_PILLS_QUANTITY";
    public static final String EXTRA_DRUG_EXPIRATION_DATE = "com.example.pamietajozdrowiu.EXTRA_DRUG_EXPIRATION_DATE";
    public static final String EXTRA_DRUG_IMAGE = "com.example.pamietajozdrowiu.EXTRA_DRUG_IMAGE";

    private TextView nameTextView, pillsQuantityTextView, expirationDateTextView, selectedTimeTextView;
    private ImageView drugImageView;
    private Button selectTimeButton, backButton, saveScheduleButton;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private List<Integer> selectedDays;
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Sprawdzenie i utworzenie kanału powiadomień
        createNotificationChannel();

        // Sprawdzenie uprawnień dla powiadomień na Androidzie 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Inicjalizacja widoków
        nameTextView = findViewById(R.id.detailNameTextView);
        pillsQuantityTextView = findViewById(R.id.detailPillsQuantityTextView);
        expirationDateTextView = findViewById(R.id.detailExpirationDateTextView);
        drugImageView = findViewById(R.id.detailDrugImageView);
        selectedTimeTextView = findViewById(R.id.selected_time_textview);
        selectTimeButton = findViewById(R.id.select_reminder_time_button);
        backButton = findViewById(R.id.button9);
        saveScheduleButton = findViewById(R.id.save_schedule_button);

        CheckBox mondayCheckbox = findViewById(R.id.checkbox_monday);
        CheckBox tuesdayCheckbox = findViewById(R.id.checkbox_tuesday);
        CheckBox wednesdayCheckbox = findViewById(R.id.checkbox_wednesday);
        CheckBox thursdayCheckbox = findViewById(R.id.checkbox_thursday);
        CheckBox fridayCheckbox = findViewById(R.id.checkbox_friday);
        CheckBox saturdayCheckbox = findViewById(R.id.checkbox_saturday);
        CheckBox sundayCheckbox = findViewById(R.id.checkbox_sunday);

        selectedDays = new ArrayList<>();

        selectTimeButton.setOnClickListener(v -> openTimePickerDialog());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DrugDetailActivity.this, YourDrugsActivity.class);
            startActivity(intent);
        });

        saveScheduleButton.setOnClickListener(v -> {
            if (selectedDays.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Wybierz dni i godzinę przypomnienia przed zapisem", Toast.LENGTH_SHORT).show();
            } else {
                saveScheduleToDatabase();
                setMedicationReminders(); // Ustawienie przypomnień
            }
        });

        mondayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.MONDAY, isChecked));
        tuesdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.TUESDAY, isChecked));
        wednesdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.WEDNESDAY, isChecked));
        thursdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.THURSDAY, isChecked));
        fridayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.FRIDAY, isChecked));
        saturdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.SATURDAY, isChecked));
        sundayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(Calendar.SUNDAY, isChecked));

        loadDrugData();
        loadScheduleFromDatabase(mondayCheckbox, tuesdayCheckbox, wednesdayCheckbox, thursdayCheckbox, fridayCheckbox, saturdayCheckbox, sundayCheckbox);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "medication_reminders",
                    "Przypomnienia o lekach",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void openTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
            selectedTimeTextView.setText("Wybrana godzina: " + selectedTime);
            Toast.makeText(this, "Godzina przypomnienia ustawiona na: " + selectedTime, Toast.LENGTH_SHORT).show();
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void toggleDaySelection(int day, boolean isSelected) {
        // Przesunięcie o jeden dzień w przód, aby MONDAY = 1, TUESDAY = 2, ..., SUNDAY = 7
        int adjustedDay = (day == Calendar.SUNDAY) ? 7 : day;

        if (isSelected) {
            if (!selectedDays.contains(adjustedDay)) {
                selectedDays.add(adjustedDay);
            }
        } else {
            selectedDays.remove(Integer.valueOf(adjustedDay));
        }
    }



    private void saveScheduleToDatabase() {
        int drugId = getIntent().getIntExtra(EXTRA_DRUG_ID, -1);

        if (drugId == -1) {
            Toast.makeText(this, "Nie można zapisać harmonogramu. Brak ID leku.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.delete("NOTIFICATION_SCHEDULE", "ID_DRUG=?", new String[]{String.valueOf(drugId)});

        for (int day : selectedDays) {
            String insertQuery = "INSERT INTO NOTIFICATION_SCHEDULE (ID_DRUG, FREQUENCY, REMINDER_TIME, START_DATE) VALUES (?, ?, ?, ?)";
            db.execSQL(insertQuery, new Object[]{drugId, day, selectedTime, getCurrentDate()});
        }
        Toast.makeText(this, "Harmonogram zapisany pomyślnie!", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setMedicationReminders() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        for (int dayOfWeek : selectedDays) {
            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

            // Korekcja, aby dni odpowiadały strukturze `Calendar.DAY_OF_WEEK`
            int daysUntilReminder = (dayOfWeek - currentDay + 7) % 7;
            if (daysUntilReminder == 0 && calendar.getTimeInMillis() > System.currentTimeMillis()) {
                daysUntilReminder += 7;
            }
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilReminder);

            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(selectedTime.split(":")[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(selectedTime.split(":")[1]));
            calendar.set(Calendar.SECOND, 0);

            Log.d("DrugDetailActivity", "Ustawianie alarmu na dzień: " + dayOfWeek + " godzina: " + selectedTime + " (data: " + calendar.getTime() + ")");

            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("drug_name", nameTextView.getText().toString());
            intent.putExtra("reminder_time", selectedTime);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, dayOfWeek, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            if (pendingIntent != null) {
                Log.d("DrugDetailActivity", "PendingIntent utworzony poprawnie.");
            } else {
                Log.e("DrugDetailActivity", "Błąd przy tworzeniu PendingIntent.");
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Log.d("DrugDetailActivity", "Alarm ustawiony: " + calendar.getTimeInMillis());
        }
    }


    private void loadScheduleFromDatabase(CheckBox mondayCheckbox, CheckBox tuesdayCheckbox, CheckBox wednesdayCheckbox,
                                          CheckBox thursdayCheckbox, CheckBox fridayCheckbox, CheckBox saturdayCheckbox,
                                          CheckBox sundayCheckbox) {
        int drugId = getIntent().getIntExtra(EXTRA_DRUG_ID, -1);

        if (drugId != -1) {
            Cursor cursor = db.rawQuery("SELECT FREQUENCY, REMINDER_TIME FROM NOTIFICATION_SCHEDULE WHERE ID_DRUG=?", new String[]{String.valueOf(drugId)});

            if (cursor.moveToFirst()) {
                do {
                    int frequency = cursor.getInt(cursor.getColumnIndexOrThrow("FREQUENCY"));
                    String reminderTime = cursor.getString(cursor.getColumnIndexOrThrow("REMINDER_TIME"));

                    switch (frequency) {
                        case Calendar.MONDAY: mondayCheckbox.setChecked(true); break;
                        case Calendar.TUESDAY: tuesdayCheckbox.setChecked(true); break;
                        case Calendar.WEDNESDAY: wednesdayCheckbox.setChecked(true); break;
                        case Calendar.THURSDAY: thursdayCheckbox.setChecked(true); break;
                        case Calendar.FRIDAY: fridayCheckbox.setChecked(true); break;
                        case Calendar.SATURDAY: saturdayCheckbox.setChecked(true); break;
                        case Calendar.SUNDAY: sundayCheckbox.setChecked(true); break;
                    }

                    selectedTime = reminderTime;
                    selectedTimeTextView.setText("Wybrana godzina: " + selectedTime);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private void loadDrugData() {
        String name = getIntent().getStringExtra(EXTRA_DRUG_NAME);
        int pillsQuantity = getIntent().getIntExtra(EXTRA_DRUG_PILLS_QUANTITY, 0);
        String expirationDate = getIntent().getStringExtra(EXTRA_DRUG_EXPIRATION_DATE);
        byte[] imageBlob = getIntent().getByteArrayExtra(EXTRA_DRUG_IMAGE);

        nameTextView.setText(name);
        pillsQuantityTextView.setText("Ilość: " + pillsQuantity);
        expirationDateTextView.setText("Termin ważności: " + expirationDate);

        if (imageBlob != null && imageBlob.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            drugImageView.setImageBitmap(bitmap);
        } else {
            drugImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Uprawnienie do powiadomień przyznane!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Uprawnienie do powiadomień jest wymagane do wyświetlania przypomnień.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
