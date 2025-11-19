package com.example.music_application.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

/**
 * Lớp User là một lớp mô hình dữ liệu (POJO) đại diện cho thông tin của một người dùng trong hệ thống.
 * Dữ liệu này thường được lưu trong Firebase Firestore hoặc Realtime Database để bổ sung thông tin
 * cho tài khoản Firebase Authentication (vốn chỉ lưu những thông tin cơ bản như email, uid).
 * @IgnoreExtraProperties giúp Firebase bỏ qua các trường không xác định khi chuyển đổi dữ liệu.
 */
@IgnoreExtraProperties
public class User {
    private String uid;          // ID duy nhất của người dùng, khớp với UID từ Firebase Authentication.
    private String username;     // Tên đăng nhập duy nhất do người dùng tự chọn.
    private String displayName;  // Tên hiển thị của người dùng.
    private String email;        // Địa chỉ email của người dùng, dùng để xác thực.
    private String role;         // Vai trò của người dùng (ví dụ: "user", "admin").
    private int playlistCount; // Số lượng playlist mà người dùng đã tạo.

    /**
     * Constructor rỗng.
     * Bắt buộc phải có để Firebase có thể tự động tạo đối tượng User từ dữ liệu lấy về.
     */
    public User() {
        // Constructor rỗng cần thiết cho Firebase
    }

    /**
     * Constructor đầy đủ tham số để tạo một đối tượng User một cách thủ công.
     * @param uid UID từ Firebase Auth.
     * @param username Tên đăng nhập.
     * @param displayName Tên hiển thị.
     * @param email Email.
     * @param role Vai trò.
     * @param playlistCount Số lượng playlist.
     */
    public User(String uid, String username, String displayName, String email, String role, int playlistCount) {
        this.uid = uid;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.playlistCount = playlistCount;
    }

    // --- Getter và Setter cho tất cả các thuộc tính ---
    // Cần thiết để các lớp khác có thể truy cập và sửa đổi dữ liệu,
    // và cũng để Firebase tự động điền dữ liệu.

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getPlaylistCount() {
        return playlistCount;
    }

    public void setPlaylistCount(int playlistCount) {
        this.playlistCount = playlistCount;
    }
}
