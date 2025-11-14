package com.example.music_application.model;

public class Song {
    private String title;
    private int resId;
    private boolean enabled;
    private String category;   // Pop, Indie, Hip-Hop, Ballad...

    public Song(String title, int resId, boolean enabled, String category) {
        this.title = title;
        this.resId = resId;
        this.enabled = enabled;
        this.category = category;
    }

    public String getTitle() { return title; }
    public int getResId() { return resId; }
    public boolean isEnabled() { return enabled; }
    public String getCategory() { return category; }

    public void setTitle(String title) { this.title = title; }
    public void setResId(int resId) { this.resId = resId; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCategory(String category) { this.category = category; }
}
