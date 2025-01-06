package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class HealtHistoryActivity extends AppCompatActivity {

    private RecyclerView healthResultsRecyclerView;
    private HealthResultsAdapter healthResultsAdapter;
    private DatabaseHelper dbHelper;
    private List<HealthResult> healthResultsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.health_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        healthResultsList = new ArrayList<>();

        // Inicjalizacja RecyclerView
        healthResultsRecyclerView = findViewById(R.id.healthResultsRecyclerView);
        healthResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        healthResultsAdapter = new HealthResultsAdapter(healthResultsList, this);
        healthResultsRecyclerView.setAdapter(healthResultsAdapter);

        // Wczytanie danych z bazy
        loadHealthResults();

        Button button = findViewById(R.id.button4);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(HealtHistoryActivity.this, MainActivity.class);
            startActivity(intent);
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(HealtHistoryActivity.this, AddHealthResultsActivity.class);
            startActivity(intent);
        });

        FloatingActionButton floatingActionButton2 = findViewById(R.id.floatingActionButton2);
        floatingActionButton2.setOnClickListener(v -> {
            Intent intent = new Intent(HealtHistoryActivity.this, SelectTutorialActivity.class);
            startActivity(intent);
        });


        FloatingActionButton floatingActionButton3 = findViewById(R.id.floatingActionButton3);
        floatingActionButton3.setOnClickListener(v -> {
            Intent intent = new Intent(HealtHistoryActivity.this, HealthReportActivity.class);
            startActivity(intent);
        });
    }

    private void loadHealthResults() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT TYPE, VALUE, DATE FROM HEALTH_RESULTS", null);

        healthResultsList.clear();
        if (cursor.moveToFirst()) {
            do {
                String type = cursor.getString(cursor.getColumnIndexOrThrow("TYPE"));
                String value = cursor.getString(cursor.getColumnIndexOrThrow("VALUE"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));

                healthResultsList.add(new HealthResult(type, value, date));
            } while (cursor.moveToNext());
        }
        cursor.close();

        healthResultsAdapter.notifyDataSetChanged();
    }
}
