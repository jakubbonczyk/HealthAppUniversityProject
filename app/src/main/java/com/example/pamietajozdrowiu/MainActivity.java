package com.example.pamietajozdrowiu;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
    }

//    Button button = findViewById(R.id.button9);
//    @SuppressLint("UseCompatLoadingForDrawables")
//    Drawable drawable = getResources().getDrawable(R.drawable.drugs);
//
//    // Ustaw rozmiar ikony w pixelach (możesz dostosować do swoich potrzeb)
//    int width = (int) getResources().getDimension(R.dimen.icon_width);
//    int height = (int) getResources().getDimension(R.dimen.icon_height);
//drawable.setBounds(0, 0, width, height);
//
//// Przypisz ikonę do buttona
//button.setCompoundDrawables(drawable, null, null, null);

}