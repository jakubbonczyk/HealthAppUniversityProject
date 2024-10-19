package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button drugsButton = findViewById(R.id.button10);
        drugsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YourDrugsActivity.class);
            startActivity(intent);
        });

        Button calendarButton = findViewById(R.id.button9);
        calendarButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        });

        Button notificationsButton = findViewById(R.id.button);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        Button healthHistoryButton = findViewById(R.id.button2);
        healthHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HealtHistoryActivity.class);
            startActivity(intent);
        });

        Button yourFeelingsButton = findViewById(R.id.button3);
        yourFeelingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, YourFeelingsActivity.class);
            startActivity(intent);
        });
    }
}