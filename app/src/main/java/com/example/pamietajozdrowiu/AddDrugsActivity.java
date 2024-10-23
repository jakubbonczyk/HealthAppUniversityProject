package com.example.pamietajozdrowiu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class AddDrugsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap takenPhotoBitmap;
    private Button takePhotoButton;
    private Button addDateButton;
    private TextInputEditText drugNameEditText;
    private TextInputEditText pillsQuantityEditText;
    private Button addButton;
    private Button cancelButton;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_drugs_activity);

        // Inicjalizacja DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase(); // Uzyskanie dostępu do zapisu w bazie danych

        // Inicjalizacja widoków
        addDateButton = findViewById(R.id.button9);
        Button backButton = findViewById(R.id.button4);
        drugNameEditText = findViewById(R.id.drugNameEditText);
        pillsQuantityEditText = findViewById(R.id.pillsQuantityEditText);
        addButton = findViewById(R.id.button12);
        cancelButton = findViewById(R.id.button11);
        takePhotoButton = findViewById(R.id.button10);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDrugsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        addDateButton.setOnClickListener(v -> openDateDialog());
        addButton.setOnClickListener(v -> addDrugToDatabase());
        cancelButton.setOnClickListener(v -> {
            clearInputs();
            Intent intent = new Intent(AddDrugsActivity.this, MainActivity.class);
            startActivity(intent);
        });
        takePhotoButton.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                takenPhotoBitmap = (Bitmap)extras.get("data");
                if (takenPhotoBitmap == null) {
                    Toast.makeText(this, "Błąd podczas pobierania zdjęcia", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private byte[] convertBitmapToBlob(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void addDrugToDatabase() {
        String drugName = drugNameEditText.getText().toString().trim();
        String pillsQuantityString = pillsQuantityEditText.getText().toString().trim();
        String expiryDate = addDateButton.getText().toString().trim();

        if (drugName.isEmpty() || pillsQuantityString.isEmpty() || expiryDate.equals("Termin ważności")) {
            Toast.makeText(this, "Proszę wypełnić wszystkie pola", Toast.LENGTH_SHORT).show();
            return;
        }

        int pillsQuantity;

        try {
            pillsQuantity = Integer.parseInt(pillsQuantityString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ilość tabletek musi być liczbą!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (takenPhotoBitmap == null) {
            Toast.makeText(this, "Proszę zrobić zdjęcie opakowania", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] imageBlob = convertBitmapToBlob(takenPhotoBitmap);

        String insertQuery = "INSERT INTO DRUGS (NAME, PILLS_QUANTITY, EXPIRATION_DATE, IMAGE) VALUES (?, ?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(insertQuery);
        statement.bindString(1, drugName);
        statement.bindLong(2, pillsQuantity);
        statement.bindString(3, expiryDate);
        statement.bindBlob(3, imageBlob);

        long rowId = statement.executeInsert();

        if (rowId != -1) {
            Toast.makeText(this, "Lek został dodany", Toast.LENGTH_SHORT).show();
            clearInputs();
        } else {
            Toast.makeText(this, "Wystąpił błąd podczas dodawania leku", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputs() {
        drugNameEditText.setText("");
        pillsQuantityEditText.setText("");
        addDateButton.setText("Termin ważności");
    }

    private void openDateDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            addDateButton.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
}
