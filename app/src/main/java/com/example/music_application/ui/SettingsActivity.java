package com.example.music_application.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.music_application.R;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREF_NAME = "museflow_prefs";
    private static final String KEY_AUTO_NEXT = "pref_auto_next";
    private static final String KEY_RESUME_LAST = "pref_resume_last";
    private static final String KEY_THEME = "pref_theme";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        Switch switchAutoNext = findViewById(R.id.switchAutoNext);
        Switch switchResumeLast = findViewById(R.id.switchResumeLast);
        RadioGroup rgTheme = findViewById(R.id.rgTheme);

        // Load saved settings
        switchAutoNext.setChecked(prefs.getBoolean(KEY_AUTO_NEXT, true));
        switchResumeLast.setChecked(prefs.getBoolean(KEY_RESUME_LAST, false));

        int savedTheme = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        updateRadioGroup(rgTheme, savedTheme);

        // Save settings on change
        switchAutoNext.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_AUTO_NEXT, isChecked).apply();
        });

        switchResumeLast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_RESUME_LAST, isChecked).apply();
        });

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = getModeFromCheckedId(checkedId);
            AppCompatDelegate.setDefaultNightMode(mode);
            prefs.edit().putInt(KEY_THEME, mode).apply();
        });
        */
    }

    private void updateRadioGroup(RadioGroup rg, int mode) {
        // This method is currently unused
    }

    private int getModeFromCheckedId(int checkedId) {
        // This method is currently unused
        return 0;
    }
}
