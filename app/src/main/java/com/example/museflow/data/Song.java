package com.example.museflow.data;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String filePath; // local path hoặc URL/ resource
    private boolean favorite;

    public Song(long id, String title, String artist, String filePath, boolean favorite) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.favorite = favorite;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getFilePath() { return filePath; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}