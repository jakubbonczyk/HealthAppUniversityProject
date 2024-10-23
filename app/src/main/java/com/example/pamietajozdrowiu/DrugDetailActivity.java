package com.example.pamietajozdrowiu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DrugDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DRUG_ID = "com.example.pamietajozdrowiu.EXTRA_DRUG_ID";
    public static final String EXTRA_DRUG_NAME = "com.example.pamietajozdrowiu.EXTRA_DRUG_NAME";
    public static final String EXTRA_DRUG_PILLS_QUANTITY = "com.example.pamietajozdrowiu.EXTRA_DRUG_PILLS_QUANTITY";
    public static final String EXTRA_DRUG_EXPIRATION_DATE = "com.example.pamietajozdrowiu.EXTRA_DRUG_EXPIRATION_DATE";
    public static final String EXTRA_DRUG_IMAGE = "com.example.pamietajozdrowiu.EXTRA_DRUG_IMAGE";

    private TextView nameTextView;
    private TextView pillsQuantityTextView;
    private TextView expirationDateTextView;
    private ImageView drugImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_detail);

        Button button = findViewById(R.id.button9);

        button.setOnClickListener(v -> {
                Intent intent = new Intent(DrugDetailActivity.this, YourDrugsActivity.class );
                startActivity(intent);
        });

        // Inicjalizacja widoków
        nameTextView = findViewById(R.id.detailNameTextView);
        pillsQuantityTextView = findViewById(R.id.detailPillsQuantityTextView);
        expirationDateTextView = findViewById(R.id.detailExpirationDateTextView);
        drugImageView = findViewById(R.id.detailDrugImageView);

        // Pobranie danych z Intent
        String name = getIntent().getStringExtra(EXTRA_DRUG_NAME);
        int pillsQuantity = getIntent().getIntExtra(EXTRA_DRUG_PILLS_QUANTITY, 0);
        String expirationDate = getIntent().getStringExtra(EXTRA_DRUG_EXPIRATION_DATE);
        byte[] imageBlob = getIntent().getByteArrayExtra(EXTRA_DRUG_IMAGE);

        // Ustawienie danych w widokach
        nameTextView.setText(name);
        pillsQuantityTextView.setText("Ilość: " + pillsQuantity);
        expirationDateTextView.setText("Termin ważności: " + expirationDate);

        if (imageBlob != null && imageBlob.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.length);
            drugImageView.setImageBitmap(bitmap);
        } else {
            drugImageView.setImageResource(R.drawable.placeholder_image);
        }
    }
}
