package com.example.music_application.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Lớp FirebaseAuthManager chịu trách nhiệm quản lý tất cả các hoạt động
 * liên quan đến xác thực người dùng với Firebase Authentication.
 * Nó đóng gói logic của Firebase, giúp cho các Activity và Fragment
 * không cần phải tương tác trực tiếp với Firebase SDK.
 */
public class FirebaseAuthManager {

    // Biến thành viên để giữ instance của FirebaseAuth, là cổng chính để tương tác với dịch vụ xác thực.
    private final FirebaseAuth mAuth;

    /**
     * Interface AuthListener hoạt động như một cơ chế callback.
     * Nó được sử dụng để thông báo cho lớp gọi (ví dụ: một Activity) về
     * kết quả của một hoạt động đăng nhập hoặc đăng ký.
     */
    public interface AuthListener {
        /**
         * Được gọi khi hoạt động xác thực (đăng nhập, đăng ký) thành công.
         * @param user Đối tượng FirebaseUser chứa thông tin của người dùng đã được xác thực.
         */
        void onSuccess(FirebaseUser user);

        /**
         * Được gọi khi hoạt động xác thực thất bại.
         * @param e Đối tượng Exception chứa thông tin về lỗi.
         */
        void onFailure(Exception e);
    }

    /**
     * Hàm khởi tạo (Constructor) của lớp FirebaseAuthManager.
     * Khi một đối tượng mới được tạo, nó sẽ lấy một instance của FirebaseAuth.
     */
    public FirebaseAuthManager() {
        // Lấy instance duy nhất (singleton) của FirebaseAuth để sử dụng trong toàn bộ lớp.
        this.mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Lấy thông tin người dùng hiện tại đang đăng nhập vào ứng dụng.
     * @return Trả về đối tượng FirebaseUser nếu có người dùng đang đăng nhập, ngược lại trả về null.
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Thực hiện quá trình đăng nhập bằng email và mật khẩu.
     * Đây là một hàm bất đồng bộ.
     * @param email Email của người dùng.
     * @param password Mật khẩu của người dùng.
     * @param listener Callback để nhận kết quả đăng nhập (thành công hoặc thất bại).
     */
    public void signIn(String email, String password, AuthListener listener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Kiểm tra xem tác vụ đăng nhập có thành công không.
                    if (task.isSuccessful()) {
                        // Nếu thành công, gọi callback onSuccess và truyền vào thông tin người dùng.
                        listener.onSuccess(mAuth.getCurrentUser());
                    } else {
                        // Nếu thất bại, gọi callback onFailure và truyền vào exception gây ra lỗi.
                        listener.onFailure(task.getException());
                    }
                });
    }

    /**
     * Thực hiện quá trình đăng ký tài khoản mới bằng email và mật khẩu.
     * Đây là một hàm bất đồng bộ.
     * @param email Email để đăng ký.
     * @param password Mật khẩu cho tài khoản mới.
     * @param listener Callback để nhận kết quả đăng ký (thành công hoặc thất bại).
     */
    public void signUp(String email, String password, AuthListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    // Kiểm tra xem tác vụ tạo người dùng có thành công không.
                    if (task.isSuccessful()) {
                        // Nếu thành công, gọi callback onSuccess.
                        listener.onSuccess(mAuth.getCurrentUser());
                    } else {
                        // Nếu thất bại, gọi callback onFailure và truyền vào exception.
                        listener.onFailure(task.getException());
                    }
                });
    }

    /**
     * Đăng xuất người dùng hiện tại ra khỏi ứng dụng.
     */
    public void signOut() {
        mAuth.signOut();
    }
}
