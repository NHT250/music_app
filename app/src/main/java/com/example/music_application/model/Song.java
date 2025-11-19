package com.example.music_application.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

/**
 * Lớp Model TỔNG HỢP cho một bài hát, đảm bảo tương thích với toàn bộ dự án.
 */
public class Song implements Serializable {

    // --- Các thuộc tính được lưu trên Firebase ---
    private String id;
    private String title;
    private String artist;
    private String url;      // Dùng cho URL nhạc MP3
    private String audioUrl; // Tương tự url, để tương thích ngược
    private String cover;    // Dùng cho URL ảnh bìa
    private String imageUrl; // Tương tự cover, để tương thích ngược
    private String category; // Thể loại nhạc
    private int likes;

    // --- Các thuộc tính chỉ dùng trên máy (không lưu lên Firebase) ---
    @Exclude
    private boolean downloaded;

    // Constructor rỗng - BẮT BUỘC cho Firebase
    public Song() {
    }

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    /**
     * Getter thông minh cho URL nhạc.
     * Ưu tiên trả về `url`, nếu không có thì trả về `audioUrl`.
     * Giúp code chạy được với cả dữ liệu cũ và mới.
     */
    public String getUrl() {
        if (url != null && !url.isEmpty()) {
            return url;
        }
        if (audioUrl != null && !audioUrl.isEmpty()) {
            return audioUrl;
        }
        return ""; // Trả về chuỗi rỗng để tránh NullPointerException
    }

    /**
     * Getter thông minh cho ảnh bìa.
     * Ưu tiên trả về `cover`, nếu không có thì trả về `imageUrl`.
     */
    public String getCover() {
        if (cover != null && !cover.isEmpty()) {
            return cover;
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        return ""; // Trả về chuỗi rỗng
    }
    
    // Các getter dự phòng để tương thích tối đa
    public String getAudioUrl() { return getUrl(); }
    public String getImageUrl() { return getCover(); }
    
    public String getCategory() { 
        return category; 
    }

    public int getLikes() {
        return likes;
    }

    @Exclude
    public boolean isDownloaded() {
        return downloaded;
    }

    // --- Setters ---
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Exclude
    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }
}
