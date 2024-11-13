package com.example.pamietajozdrowiu;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;

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

        // Inicjalizacja widoków
        nameTextView = findViewById(R.id.detailNameTextView);
        pillsQuantityTextView = findViewById(R.id.detailPillsQuantityTextView);
        expirationDateTextView = findViewById(R.id.detailExpirationDateTextView);
        drugImageView = findViewById(R.id.detailDrugImageView);
        selectedTimeTextView = findViewById(R.id.selected_time_textview);
        selectTimeButton = findViewById(R.id.select_reminder_time_button);
        backButton = findViewById(R.id.button9);
        saveScheduleButton = findViewById(R.id.save_schedule_button);

        // Inicjalizacja checkboxów
        CheckBox mondayCheckbox = findViewById(R.id.checkbox_monday);
        CheckBox tuesdayCheckbox = findViewById(R.id.checkbox_tuesday);
        CheckBox wednesdayCheckbox = findViewById(R.id.checkbox_wednesday);
        CheckBox thursdayCheckbox = findViewById(R.id.checkbox_thursday);
        CheckBox fridayCheckbox = findViewById(R.id.checkbox_friday);
        CheckBox saturdayCheckbox = findViewById(R.id.checkbox_saturday);
        CheckBox sundayCheckbox = findViewById(R.id.checkbox_sunday);

        selectedDays = new ArrayList<>();

        // Listener dla przycisku wyboru godziny
        selectTimeButton.setOnClickListener(v -> openTimePickerDialog());

        // Listener dla przycisku Wróć
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(DrugDetailActivity.this, YourDrugsActivity.class);
            startActivity(intent);
        });

        // Listener dla przycisku zapisu harmonogramu
        saveScheduleButton.setOnClickListener(v -> {
            if (selectedDays.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this, "Wybierz dni i godzinę przypomnienia przed zapisem", Toast.LENGTH_SHORT).show();
            } else {
                saveScheduleToDatabase();
            }
        });

        // Listener dla checkboxów
        mondayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(1, isChecked));
        tuesdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(2, isChecked));
        wednesdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(3, isChecked));
        thursdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(4, isChecked));
        fridayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(5, isChecked));
        saturdayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(6, isChecked));
        sundayCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDaySelection(7, isChecked));

        // Pobranie danych z Intent i ustawienie widoku
        loadDrugData();
        loadScheduleFromDatabase(mondayCheckbox, tuesdayCheckbox, wednesdayCheckbox, thursdayCheckbox, fridayCheckbox, saturdayCheckbox, sundayCheckbox); // Załadowanie harmonogramu z bazy danych
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
        if (isSelected) {
            selectedDays.add(day);
        } else {
            selectedDays.remove(Integer.valueOf(day));
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
                        case 1: mondayCheckbox.setChecked(true); break;
                        case 2: tuesdayCheckbox.setChecked(true); break;
                        case 3: wednesdayCheckbox.setChecked(true); break;
                        case 4: thursdayCheckbox.setChecked(true); break;
                        case 5: fridayCheckbox.setChecked(true); break;
                        case 6: saturdayCheckbox.setChecked(true); break;
                        case 7: sundayCheckbox.setChecked(true); break;
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
}
