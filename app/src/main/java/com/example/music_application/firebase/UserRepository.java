package com.example.music_application.firebase;

import androidx.annotation.NonNull;

import com.example.music_application.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Lớp UserRepository đóng vai trò là một lớp trung gian (repository) để quản lý
 * tất cả các tương tác với Firebase Realtime Database liên quan đến đối tượng User.
 * Lớp này xử lý việc tạo, đọc, và cập nhật thông tin người dùng bổ sung như
 * username, role, displayName, v.v.
 */
public class UserRepository {

    // Tham chiếu đến node "users" trong Firebase Realtime Database.
    private final DatabaseReference databaseReference;

    /**
     * Interface callback để trả về kết quả của việc lấy thông tin một người dùng.
     */
    public interface UserListener {
        void onUserLoaded(User user); // Được gọi khi tải thông tin người dùng thành công.
        void onError(Exception e);    // Được gọi khi có lỗi xảy ra.
    }

    /**
     * Constructor của lớp.
     * Lấy tham chiếu đến node "users" trong Realtime Database.
     */
    public UserRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    /**
     * Tạo một bản ghi người dùng mới trong database. Thường được gọi sau khi đăng ký thành công.
     * @param user Đối tượng User chứa thông tin cần lưu.
     * @param onSuccess Callback khi thành công.
     * @param onFailure Callback khi thất bại.
     */
    public void createUser(User user, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        // Sử dụng UID của người dùng từ Firebase Auth làm key cho bản ghi trong Realtime Database.
        databaseReference.child(user.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    /**
     * Lấy thông tin chi tiết của một người dùng dựa vào UID.
     * @param uid UID của người dùng cần tìm.
     * @param listener Callback để nhận về đối tượng User hoặc lỗi.
     */
    public void getUser(String uid, UserListener listener) {
        databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Nếu snapshot tồn tại, chuyển nó thành đối tượng User và trả về.
                    User user = snapshot.getValue(User.class);
                    listener.onUserLoaded(user);
                } else {
                    // Nếu không tìm thấy, trả về lỗi.
                    listener.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Tìm kiếm một người dùng dựa trên username. Đây là chức năng quan trọng cho phép
     * người dùng đăng nhập bằng username thay vì email.
     * @param username Tên đăng nhập cần tìm.
     * @param listener Callback để nhận về đối tượng User hoặc lỗi.
     */
    public void findUserByUsername(String username, UserListener listener) {
        // Tạo một truy vấn để tìm các bản ghi có trường "username" bằng với giá trị được cung cấp.
        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Vì username là duy nhất, ta chỉ cần lấy bản ghi đầu tiên tìm thấy.
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        listener.onUserLoaded(user);
                        return; // Dừng lại sau khi tìm thấy.
                    }
                } else {
                    // Nếu không có snapshot nào tồn tại, có nghĩa là không tìm thấy username.
                    listener.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    /**
     * Cập nhật thông tin của một người dùng đã tồn tại.
     * @param user Đối tượng User chứa thông tin mới.
     * @param onSuccess Callback khi thành công.
     * @param onFailure Callback khi thất bại.
     */
    public void updateUser(User user, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(user.getUid()).setValue(user)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }
}
