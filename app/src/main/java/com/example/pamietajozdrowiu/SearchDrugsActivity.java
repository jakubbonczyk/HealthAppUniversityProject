package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchDrugsActivity extends AppCompatActivity {

    private Spinner sicknessSearchSpinner;
    private RecyclerView searchResultsRecyclerView;
    private DrugAdapter drugAdapter;
    private List<Drug> drugList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_drug_activity);

        dbHelper = new DatabaseHelper(this);
        Button button = findViewById(R.id.button4);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(SearchDrugsActivity.this, YourDrugsActivity.class);
            startActivity(intent);
        });

        // Inicjalizacja widoków
        sicknessSearchSpinner = findViewById(R.id.sicknessSearchSpinner);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSicknessData(); // Ładowanie danych do Spinnera

        // Inicjalizacja SearchButton i przypisanie listenera
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> searchDrugsBySickness()); // Przypisanie funkcji wyszukiwania do przycisku
    }

    private void loadSicknessData() {
        List<String> sicknessList = new ArrayList<>();
        sicknessList.add("Wybierz grupę schorzeń"); // Placeholder

        // Wczytaj grupy schorzeń z bazy danych
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT NAME FROM SICKNESSES", null);
        if (cursor.moveToFirst()) {
            do {
                String sicknessName = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                sicknessList.add(sicknessName);
            } while (cursor.moveToNext());
        }
        cursor.close();

        // Ustawienie adaptera dla Spinnera z placeholderem
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
        sicknessSearchSpinner.setAdapter(adapter);
    }

    private void searchDrugsBySickness() {
        String selectedSickness = sicknessSearchSpinner.getSelectedItem().toString();
        drugList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT d.ID_DRUG, d.NAME, d.PILLS_QUANTITY, d.EXPIRATION_DATE, d.IMAGE " +
                        "FROM DRUGS d INNER JOIN SICKNESSES s ON d.ID_SICKNESS = s.ID_SICKNESS " +
                        "WHERE s.NAME = ?", new String[]{selectedSickness});

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

        drugAdapter = new DrugAdapter(drugList, this);
        searchResultsRecyclerView.setAdapter(drugAdapter);
    }
}
