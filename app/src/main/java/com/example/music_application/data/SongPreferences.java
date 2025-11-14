package com.example.music_application.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SongPreferences {

    private static final String PREF_NAME = "song_prefs";
    private final SharedPreferences prefs;

    public SongPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    private String keyEnabled(int resId) { return "song_enabled_" + resId; }
    private String keyTitle(int resId)   { return "song_title_" + resId; }
    private String keyCategory(int resId){ return "song_category_" + resId; }

    public boolean isSongEnabled(int resId) {
        return prefs.getBoolean(keyEnabled(resId), false); // Changed back to false
    }

    public void setSongEnabled(int resId, boolean enabled) {
        prefs.edit().putBoolean(keyEnabled(resId), enabled).apply();
    }

    public String getSongTitle(int resId, String defaultTitle) {
        return prefs.getString(keyTitle(resId), defaultTitle);
    }

    public void setSongTitle(int resId, String title) {
        prefs.edit().putString(keyTitle(resId), title).apply();
    }

    public void clearSongTitle(int resId) {
        prefs.edit().remove(keyTitle(resId)).apply();
    }

    // ---- Category ----
    public String getSongCategory(int resId, String defaultCategory) {
        return prefs.getString(keyCategory(resId), defaultCategory);
    }

    public void setSongCategory(int resId, String category) {
        prefs.edit().putString(keyCategory(resId), category).apply();
    }

    public void clearSongCategory(int resId) {
        prefs.edit().remove(keyCategory(resId)).apply();
    }
}