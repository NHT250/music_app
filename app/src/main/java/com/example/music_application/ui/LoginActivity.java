package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.UserRepository;
import com.example.music_application.model.User;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

/**
 * LoginActivity là màn hình xử lý việc đăng nhập của người dùng.
 * Chức năng chính:
 * - Cho phép người dùng nhập tên người dùng (username) và mật khẩu.
 * - Xử lý logic đăng nhập bằng cách kết hợp tra cứu username trong Firestore và xác thực bằng email/password với Firebase Auth.
 * - Điều hướng người dùng đến màn hình tương ứng (Admin hoặc User) sau khi đăng nhập thành công.
 * - Cung cấp tùy chọn chuyển đến màn hình đăng ký.
 */
public class LoginActivity extends AppCompatActivity {

    // Các biến cho thành phần giao diện người dùng.
    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView txtGoRegister;

    // Các lớp quản lý dữ liệu cho Firebase.
    private FirebaseAuthManager authManager; // Quản lý xác thực (Auth).
    private UserRepository userRepository; // Quản lý dữ liệu người dùng trong Firestore.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các trình quản lý.
        authManager = new FirebaseAuthManager();
        userRepository = new UserRepository();

        // Ánh xạ các view từ layout.
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtGoRegister = findViewById(R.id.txtGoRegister);

        // Thiết lập sự kiện click cho nút đăng nhập và text chuyển sang đăng ký.
        btnLogin.setOnClickListener(v -> loginWithUsername());
        txtGoRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    /**
     * Xử lý logic đăng nhập khi người dùng nhấn nút "Login".
     * Quy trình gồm 2 bước: 
     * 1. Tìm email thật của người dùng dựa vào username nhập vào.
     * 2. Dùng email đó và mật khẩu để đăng nhập bằng Firebase Auth.
     */
    private void loginWithUsername() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Kiểm tra đầu vào cơ bản.
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Tìm người dùng trong Firestore bằng username.
        userRepository.findUserByUsername(username, new UserRepository.UserListener() {
            @Override
            public void onUserLoaded(User user) {
                // Bước 2: Nếu tìm thấy username, lấy email thật và tiến hành đăng nhập bằng Firebase Auth.
                if (user != null && user.getEmail() != null) {
                    authManager.signIn(user.getEmail(), password, new FirebaseAuthManager.AuthListener() {
                        @Override
                        public void onSuccess(com.google.firebase.auth.FirebaseUser firebaseUser) {
                            // Đăng nhập thành công, tiếp tục kiểm tra vai trò (role) của người dùng.
                            checkUserRole(firebaseUser.getUid());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // Lỗi ở bước này thường là do sai mật khẩu.
                             if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                // Trường hợp user == null đã được xử lý trong onError của findUserByUsername.
            }

            @Override
            public void onError(Exception e) {
                // Lỗi ở bước này là do không tìm thấy username trong Firestore.
                if ("User not found".equals(e.getMessage())) {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sau khi đăng nhập thành công, kiểm tra vai trò của người dùng (admin hay user)
     * để điều hướng đến màn hình phù hợp.
     * @param uid User ID của người dùng vừa đăng nhập.
     */
    private void checkUserRole(String uid) {
        userRepository.getUser(uid, new UserRepository.UserListener() {
            @Override
            public void onUserLoaded(User user) {
                if (user != null) {
                    // Dựa vào trường "role" để quyết định màn hình tiếp theo.
                    if ("admin".equals(user.getRole())) {
                        startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, UserHomeActivity.class));
                    }
                    finish(); // Đóng LoginActivity để người dùng không thể quay lại.
                } else {
                     Toast.makeText(LoginActivity.this, "Could not retrieve user data.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(LoginActivity.this, "Failed to get user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
