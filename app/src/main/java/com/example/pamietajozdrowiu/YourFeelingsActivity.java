package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class YourFeelingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.your_feelings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button = findViewById(R.id.button4);
        Button sadButton = findViewById(R.id.button5);
        Button neutralButton = findViewById(R.id.button6);
        Button happyButton = findViewById(R.id.button7);

        button.setOnClickListener(v -> {
            Intent intent = new Intent(YourFeelingsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        sadButton.setOnClickListener(v -> {
            Toast.makeText(this, "Czujesz się źle!", Toast.LENGTH_SHORT).show();
        });

        neutralButton.setOnClickListener(v -> {
            Toast.makeText(this, "Czujesz się neutralnie!", Toast.LENGTH_SHORT).show();
        });

        happyButton.setOnClickListener(v -> {
            Toast.makeText(this, "Czujesz się świetnie!", Toast.LENGTH_SHORT).show();
        });
    }
}