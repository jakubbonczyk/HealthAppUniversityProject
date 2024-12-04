package com.example.pamietajozdrowiu;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddDrugsActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final String EXTRA_DRUG_IMAGE = "com.example.pamietajozdrowiu.EXTRA_DRUG_IMAGE";
    private Bitmap takenPhotoBitmap;
    private Button takePhotoButton;
    private Button addDateButton;
    private ImageView drugImageView;
    private TextInputEditText drugNameEditText;
    private TextInputEditText pillsQuantityEditText;
    private Button addButton;
    private Button cancelButton;
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Spinner sicknessSpinner;
    private List<String> sicknessList;

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
        drugImageView = findViewById(R.id.detailDrugImageView);
        addButton = findViewById(R.id.button12);
        cancelButton = findViewById(R.id.button11);
        takePhotoButton = findViewById(R.id.button10);
        sicknessSpinner = findViewById(R.id.sicknessSearchSpinner);
        loadSicknessData();

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
                takenPhotoBitmap = (Bitmap) extras.get("data");
                if (takenPhotoBitmap != null) {
                    drugImageView.setImageBitmap(takenPhotoBitmap);
                } else {
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
        String selectedSickness = sicknessSpinner.getSelectedItem().toString();

        if (selectedSickness.equals("Wybierz grupę schorzeń")) {
            Toast.makeText(this, "Proszę wybrać grupę schorzeń", Toast.LENGTH_SHORT).show();
            return;
        }

        int sicknessId = getSicknessId(selectedSickness);

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

        String insertQuery = "INSERT INTO DRUGS (NAME, PILLS_QUANTITY, EXPIRATION_DATE, IMAGE, ID_SICKNESS) VALUES (?, ?, ?, ?, ?)";
        SQLiteStatement statement = db.compileStatement(insertQuery);
        statement.bindString(1, drugName);
        statement.bindLong(2, pillsQuantity);
        statement.bindString(3, expiryDate);
        statement.bindBlob(4, imageBlob);
        statement.bindLong(5, sicknessId);

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
        sicknessSpinner.setSelection(0);
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

    private void loadSicknessData() {
        sicknessList = new ArrayList<>();
        sicknessList.add("Wybierz grupę schorzeń"); // Placeholder

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SICKNESSES", null);

        if (cursor.moveToFirst()) {
            do {
                String sicknessName = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                sicknessList.add(sicknessName);
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, sicknessList) {
            @Override
            public boolean isEnabled(int position) {
                // Pierwszy element ("Wybierz grupę schorzeń") ma być nieaktywny
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Ustaw kolor tekstu dla placeholdera
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Placeholder powinien być szary
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.WHITE);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.spinner_item);
        sicknessSpinner.setAdapter(adapter);
    }

    private int getSicknessId(String sicknessName) {
        int sicknessId = -1;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ID_SICKNESS FROM SICKNESSES WHERE NAME = ?", new String[]{sicknessName});
        if (cursor.moveToFirst()) {
            sicknessId = cursor.getInt(cursor.getColumnIndexOrThrow("ID_SICKNESS"));
        }
        cursor.close();
        return sicknessId;
    }
}
