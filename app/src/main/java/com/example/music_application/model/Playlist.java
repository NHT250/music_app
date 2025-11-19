package com.example.music_application.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

/**
 * Lớp Playlist là một lớp mô hình dữ liệu (POJO) đại diện cho một danh sách phát (playlist) của người dùng.
 * Nó định nghĩa cấu trúc của một playlist, bao gồm thông tin cơ bản và danh sách các bài hát thuộc về nó.
 * @IgnoreExtraProperties giúp Firebase bỏ qua các trường không xác định khi chuyển đổi dữ liệu.
 */
@IgnoreExtraProperties
public class Playlist {
    private String id;            // ID duy nhất của playlist.
    private String title;         // Tên của playlist do người dùng đặt.
    private String ownerId;       // ID của người dùng sở hữu playlist này.
    private List<String> songIds; // Danh sách các ID của bài hát có trong playlist. Lưu ý: chỉ lưu ID để tối ưu dữ liệu, không lưu toàn bộ đối tượng Song.

    /**
     * Constructor rỗng.
     * Bắt buộc phải có để Firebase có thể tự động tạo đối tượng Playlist từ dữ liệu lấy về.
     */
    public Playlist() {
        // Constructor rỗng cần thiết cho Firebase
    }

    /**
     * Constructor đầy đủ tham số để tạo một đối tượng Playlist một cách thủ công.
     * @param id ID của playlist.
     * @param title Tên playlist.
     * @param ownerId ID của người sở hữu.
     * @param songIds Danh sách ID các bài hát.
     */
    public Playlist(String id, String title, String ownerId, List<String> songIds) {
        this.id = id;
        this.title = title;
        this.ownerId = ownerId;
        this.songIds = songIds;
    }

    // --- Getter và Setter cho tất cả các thuộc tính ---
    // Cần thiết để các lớp khác có thể truy cập và sửa đổi dữ liệu,
    // và cũng để Firebase tự động điền dữ liệu.

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
}
