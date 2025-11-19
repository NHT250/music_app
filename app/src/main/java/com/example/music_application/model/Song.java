package com.example.music_application.model;

import android.content.Context;
import com.google.firebase.database.Exclude;
import java.io.Serializable;

public class Song implements Serializable {

    // --- Firebase Attributes (original and new) ---
    private String id;
    private String title;
    private String artist;
    private String url;      // Main field for audio URL
    private String audioUrl; // For backward compatibility
    private String cover;    // Main field for cover image URL
    private String imageUrl; // For backward compatibility
    private String category;
    private int likes;

    // --- Local-only Attributes (Not for Firebase) ---
    @Exclude
    private String songResourceName; // For local raw audio files
    @Exclude
    private String imageResourceName; // For local drawable images
    @Exclude
    private boolean downloaded;

    // --- Constructors ---
    public Song() { // Required empty constructor for Firebase
    }

    // Constructor for local songs with resource names
    public Song(int id, String title, String artist, String songResourceName, String imageResourceName) {
        this.id = String.valueOf(id);
        this.title = title;
        this.artist = artist;
        this.songResourceName = songResourceName;
        this.imageResourceName = imageResourceName;
    }

    // --- Intelligent Getters (Restored from original) ---
    public String getUrl() {
        if (url != null && !url.isEmpty()) return url;
        if (audioUrl != null && !audioUrl.isEmpty()) return audioUrl;
        return "";
    }

    public String getCover() {
        if (cover != null && !cover.isEmpty()) return cover;
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        return "";
    }

    // --- Compatibility Getters (To fix the build errors) ---
    public String getAudioUrl() { return getUrl(); }
    public String getImageUrl() { return getCover(); }
    
    // --- Standard Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getCategory() { return category; }
    public int getLikes() { return likes; }
    @Exclude
    public boolean isDownloaded() { return downloaded; }

    // --- Standard Setters ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setUrl(String url) { this.url = url; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setCover(String cover) { this.cover = cover; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setLikes(int likes) { this.likes = likes; }
    @Exclude
    public void setDownloaded(boolean downloaded) { this.downloaded = downloaded; }

    // --- Methods for Local Resources ---
    @Exclude
    public int getSongResourceId(Context context) {
        if (songResourceName == null || songResourceName.isEmpty()) return 0;
        return context.getResources().getIdentifier(songResourceName, "raw", context.getPackageName());
    }

    @Exclude
    public int getImageResourceId(Context context) {
        if (imageResourceName == null || imageResourceName.isEmpty()) return 0;
        return context.getResources().getIdentifier(imageResourceName, "drawable", context.getPackageName());
    }
}
