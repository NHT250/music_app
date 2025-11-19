package com.example.music_application.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.music_application.R;
import com.example.music_application.util.ThemeManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "SettingsPrefs";
    private static final String AUTO_NEXT_KEY = "autoNext";
    private static final String RESUME_LAST_KEY = "resumeLast";

    private SwitchMaterial switchAutoNext, switchResumeLast;
    private RadioGroup rgTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        switchAutoNext = findViewById(R.id.switch_auto_next);
        switchResumeLast = findViewById(R.id.switch_resume_last);
        rgTheme = findViewById(R.id.rg_theme);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        switchAutoNext.setChecked(prefs.getBoolean(AUTO_NEXT_KEY, true));
        switchResumeLast.setChecked(prefs.getBoolean(RESUME_LAST_KEY, false));

        switch (ThemeManager.getTheme(this)) {
            case ThemeManager.THEME_LIGHT:
                rgTheme.check(R.id.rb_light);
                break;
            case ThemeManager.THEME_DARK:
                rgTheme.check(R.id.rb_dark);
                break;
            case ThemeManager.THEME_AUTO:
                rgTheme.check(R.id.rb_auto);
                break;
        }
    }

    private void setupListeners() {
        switchAutoNext.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean(AUTO_NEXT_KEY, isChecked);
        });

        switchResumeLast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveBoolean(RESUME_LAST_KEY, isChecked);
        });

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_light) {
                ThemeManager.setTheme(this, ThemeManager.THEME_LIGHT);
            } else if (checkedId == R.id.rb_dark) {
                ThemeManager.setTheme(this, ThemeManager.THEME_DARK);
            } else if (checkedId == R.id.rb_auto) {
                ThemeManager.setTheme(this, ThemeManager.THEME_AUTO);
            }
            recreate();
        });
    }

    private void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
}
