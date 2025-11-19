package com.example.music_application.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {

    private static final String PREFS_NAME = "MuseFlowSettings";
    private static final String KEY_AUTO_NEXT = "autoNext";
    private static final String KEY_RESUME_LAST = "resumeLast";
    private static final String KEY_THEME = "theme";

    private final SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void setAutoNext(boolean autoNext) {
        sharedPreferences.edit().putBoolean(KEY_AUTO_NEXT, autoNext).apply();
    }

    public boolean isAutoNextEnabled() {
        return sharedPreferences.getBoolean(KEY_AUTO_NEXT, true); // Default to true
    }

    public void setResumeLast(boolean resumeLast) {
        sharedPreferences.edit().putBoolean(KEY_RESUME_LAST, resumeLast).apply();
    }

    public boolean isResumeLastEnabled() {
        return sharedPreferences.getBoolean(KEY_RESUME_LAST, false); // Default to false
    }

    public void setTheme(int themeMode) {
        sharedPreferences.edit().putInt(KEY_THEME, themeMode).apply();
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    public int getTheme() {
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
