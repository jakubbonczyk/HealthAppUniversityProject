package com.example.pamietajozdrowiu;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddHealthResultsActivity extends AppCompatActivity {

    private Spinner resultTypeSpinner;
    private TextInputEditText resultValueEditText;
    private Button saveButton, backButton;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health_results);

        dbHelper = new DatabaseHelper(this);

        resultTypeSpinner = findViewById(R.id.resultTypeSpinner);
        resultValueEditText = findViewById(R.id.resultValueEditText);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        // Ustawienie Spinnera z typami badań
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.health_result_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resultTypeSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> saveHealthResult());
        backButton.setOnClickListener(v -> finish());
    }

    private void saveHealthResult() {
        String type = resultTypeSpinner.getSelectedItem().toString();
        String value = resultValueEditText.getText().toString();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if ("Wybierz typ badania".equals(type) || value.isEmpty()) {
            Toast.makeText(this, "Proszę wybrać typ badania i podać wartość!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String query = "INSERT INTO HEALTH_RESULTS (ID_USER, TYPE, VALUE, DATE) VALUES (?,?,?,?)";
        db.execSQL(query, new Object[]{1, type, value, date}); // Zakładamy ID_USER = 1
        Toast.makeText(this, "Wynik zapisany!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
