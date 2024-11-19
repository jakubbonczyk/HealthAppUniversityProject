package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MedicationIntakeActivity extends AppCompatActivity {

    private RecyclerView medicationsRecyclerView;
    private MedicationIntakeAdapter medicationIntakeAdapter;
    private DatabaseHelper dbHelper;
    private ArrayList<Drug> drugList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_intake);

        dbHelper = new DatabaseHelper(this);

        medicationsRecyclerView = findViewById(R.id.medicationsRecyclerView);
        medicationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadMedications();

        Button backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MedicationIntakeActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void loadMedications() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM DRUGS", null);

        drugList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID_DRUG"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                String expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPIRATION_DATE"));
                byte[] imageBlob = cursor.getBlob(cursor.getColumnIndexOrThrow("IMAGE"));

                Drug drug = new Drug(id, name, pillsQuantity, expirationDate, imageBlob);
                drugList.add(drug);
            } while (cursor.moveToNext());
        }
        cursor.close();

        medicationIntakeAdapter = new MedicationIntakeAdapter(drugList, this);
        medicationsRecyclerView.setAdapter(medicationIntakeAdapter);
    }
}
