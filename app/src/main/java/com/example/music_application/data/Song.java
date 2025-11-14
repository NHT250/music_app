package com.example.music_application.data;

public class Song {
    private long id;
    private String title;
    private String artist;
    private String filePath;
    private int resId;
    private boolean favorite;
    private String coverPath;
    private String category;

    public Song(long id, String title, String artist, String filePath, int resId, boolean favorite, String coverPath, String category) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.resId = resId;
        this.favorite = favorite;
        this.coverPath = coverPath;
        this.category = category;
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getFilePath() { return filePath; }
    public int getResId() { return resId; }
    public boolean isFavorite() { return favorite; }
    public String getCoverPath() { return coverPath; }
    public String getCategory() { return category; }

    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }
}
