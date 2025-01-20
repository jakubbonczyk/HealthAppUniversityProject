package com.example.pamietajozdrowiu;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FeelingsHistoryActivity extends AppCompatActivity {

    private RecyclerView feelingsRecyclerView;
    private FeelingsHistoryAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Feeling> feelingsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feelings_history);

        feelingsRecyclerView = findViewById(R.id.feelingsRecyclerView);
        feelingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DatabaseHelper(this);
        loadFeelingsHistory();
    }

    private void loadFeelingsHistory() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT NAME, DATE, NOTES FROM USER_FEELINGS ORDER BY DATE DESC"; // Pobiera wszystkie wpisy, sortując je malejąco po dacie

        Cursor cursor = db.rawQuery(query, null);
        feelingsList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("NAME"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("DATE"));
                String notes = cursor.getString(cursor.getColumnIndexOrThrow("NOTES"));

                feelingsList.add(new Feeling(name, date, notes)); // Dodajemy każdy wpis do listy
                Log.d("FeelingsHistory", "NAME: " + name + ", DATE: " + date + ", NOTES: " + notes);

            } while (cursor.moveToNext());
        } else {
            Toast.makeText(this, "Brak zapisanej historii.", Toast.LENGTH_SHORT).show();
        }
        cursor.close();

        // Tworzenie adaptera i ustawienie go w RecyclerView
        adapter = new FeelingsHistoryAdapter(feelingsList);
        feelingsRecyclerView.setAdapter(adapter);
    }

}
