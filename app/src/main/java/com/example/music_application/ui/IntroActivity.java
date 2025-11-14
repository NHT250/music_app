package com.example.music_application.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;

public class    IntroActivity extends AppCompatActivity {

    private static final String PREF_AUTH = "museflow_auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Delay 1.2s cho đẹp (hoặc bỏ Handler, điều hướng luôn)
        new Handler().postDelayed(this::navigateNext, 1200);
    }

    private void navigateNext() {
        SharedPreferences prefs = getSharedPreferences(PREF_AUTH, MODE_PRIVATE);
        boolean loggedIn = prefs.getBoolean(KEY_LOGGED_IN, false);
        long userId = prefs.getLong(KEY_USER_ID, -1);
        String role = prefs.getString(KEY_ROLE, "user");

        if (loggedIn && userId != -1) {
            if ("admin".equalsIgnoreCase(role)) {
                Intent i = new Intent(this, AdminDashboardActivity.class);
                i.putExtra("user_id", userId);
                startActivity(i);
            } else {
                Intent i = new Intent(this, UserHomeActivity.class);
                i.putExtra("user_id", userId);
                startActivity(i);
            }
        } else {
            // chưa login → đi tới màn login
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        }

        finish();
    }
}