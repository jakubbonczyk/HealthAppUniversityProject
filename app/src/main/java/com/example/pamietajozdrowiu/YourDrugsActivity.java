package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class YourDrugsActivity extends AppCompatActivity {

    private RecyclerView drugsRecyclerView;
    private DrugAdapter drugAdapter;
    private DatabaseHelper dbHelper;
    private List<Drug> drugList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_your_drugs);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button backButton = findViewById(R.id.button4);
        FloatingActionButton addButton = findViewById(R.id.floatingActionButton);
        drugsRecyclerView = findViewById(R.id.drugsRecyclerView);
        drugsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        loadDrugsFromDatabase();

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(YourDrugsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(YourDrugsActivity.this, AddDrugsActivity.class);
            startActivity(intent);
        });
    }

    private void loadDrugsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM DRUGS", null);

        drugList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("ID_DRUG"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                int pillsQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("PILLS_QUANTITY"));
                String expirationDate = cursor.getString(cursor.getColumnIndexOrThrow("EXPIRATION_DATE"));

                Drug drug = new Drug(id, name, pillsQuantity, expirationDate);
                drugList.add(drug);
            } while (cursor.moveToNext());
        }
        cursor.close();

        drugAdapter = new DrugAdapter(drugList, this);
        drugsRecyclerView.setAdapter(drugAdapter);

    }
}
