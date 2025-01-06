package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SelectTutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tutorial);

        Button pressureTutorialButton = findViewById(R.id.pressureTutorialButton);
        Button temperatureTutorialButton = findViewById(R.id.temperatureTutorialButton);

        pressureTutorialButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectTutorialActivity.this, TutorialActivity.class);
            intent.putExtra("tutorial", "pressure");
            startActivity(intent);
        });

        temperatureTutorialButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectTutorialActivity.this, TutorialActivity.class);
            intent.putExtra("tutorial", "temperature");
            startActivity(intent);
        });
    }
}
