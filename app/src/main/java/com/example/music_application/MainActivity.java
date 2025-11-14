package com.example.music_application;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.ui.IntroActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Immediately redirect to IntroActivity, which handles the main logic
        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);
        finish(); // Finish MainActivity so user can't navigate back to it
    }
}
