package com.example.pamietajozdrowiu;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class TutorialActivity extends AppCompatActivity {
    private VideoView tutorialVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        tutorialVideoView = findViewById(R.id.tutorialVideoView);

        String tutorialType = getIntent().getStringExtra("tutorial");
        int videoResId;

        if ("temperature".equals(tutorialType)) {
            videoResId = R.raw.tutorial2; // Plik w res/raw
        } else {
            videoResId = R.raw.tutorial; // Domyślnie pomiar ciśnienia
        }

        String videoPath = "android.resource://" + getPackageName() + "/" + videoResId;
        tutorialVideoView.setVideoURI(Uri.parse(videoPath));

        // Dodanie MediaController
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(tutorialVideoView);
        tutorialVideoView.setMediaController(mediaController);

        // Tryb pełnoekranowy
        tutorialVideoView.setOnClickListener(v -> toggleFullscreen());

        tutorialVideoView.start();
    }

    private void toggleFullscreen() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }
}
