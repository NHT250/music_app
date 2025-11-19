package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.UserRepository;
import com.example.music_application.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * RegisterActivity là màn hình cho phép người dùng mới tạo tài khoản.
 * Quy trình đăng ký phức tạp hơn một chút, bao gồm các bước:
 * 1. Kiểm tra xem username đã tồn tại hay chưa.
 * 2. Xác định vai trò (role) cho người dùng mới. Người dùng đầu tiên sẽ là 'admin', những người sau là 'user'.
 * 3. Tạo tài khoản trong Firebase Authentication (sử dụng email và mật khẩu).
 * 4. Lưu thông tin đầy đủ của người dùng (bao gồm username, role, ...) vào Firebase Realtime Database.
 */
public class RegisterActivity extends AppCompatActivity {

    // Các biến cho thành phần giao diện người dùng.
    private EditText edtDisplayName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;

    // Các lớp quản lý dữ liệu Firebase.
    private FirebaseAuthManager authManager;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo các trình quản lý.
        authManager = new FirebaseAuthManager();
        userRepository = new UserRepository();

        // Ánh xạ các view từ layout.
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        // Thiết lập sự kiện click cho nút đăng ký và text chuyển về màn hình đăng nhập.
        btnRegister.setOnClickListener(v -> registerUser());
        findViewById(R.id.txtGoLogin).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /**
     * Bắt đầu quy trình đăng ký người dùng sau khi nhấn nút.
     */
    private void registerUser() {
        // Lấy dữ liệu từ các ô nhập liệu.
        String displayName = edtDisplayName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra các điều kiện đầu vào.
        if (displayName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Kiểm tra xem username đã tồn tại chưa bằng cách tìm kiếm trong database.
        userRepository.findUserByUsername(username, new UserRepository.UserListener() {
            @Override
            public void onUserLoaded(User user) {
                // Nếu onUserLoaded được gọi, có nghĩa là đã tìm thấy user với username này.
                // Đây là trường hợp lỗi, username phải là duy nhất.
                Toast.makeText(RegisterActivity.this, "Username already exists. Please choose another one.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                // Nếu onError được gọi với thông báo "User not found", đây là điều ta mong muốn.
                // Nó có nghĩa là username này chưa có ai sử dụng và có thể tiếp tục đăng ký.
                if ("User not found".equals(e.getMessage())) {
                    // Bước 2: Username hợp lệ, tiếp tục xác định vai trò và đăng ký.
                    determineRoleAndRegister(displayName, username, email, password);
                } else {
                    // Một lỗi khác đã xảy ra trong quá trình kiểm tra username.
                    Toast.makeText(RegisterActivity.this, "Error checking username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Xác định vai trò cho người dùng. Nếu chưa có người dùng nào trong hệ thống,
     * người dùng mới này sẽ có vai trò 'admin'. Ngược lại, vai trò sẽ là 'user'.
     */
    private void determineRoleAndRegister(String displayName, String username, String email, String password) {
        // Kiểm tra xem node "users" trong Realtime Database có tồn tại và có con không.
        FirebaseDatabase.getInstance().getReference("users").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = "user"; // Mặc định vai trò là 'user'.
                if (!snapshot.exists()) {
                    // Nếu snapshot không tồn tại, tức là chưa có user nào -> gán vai trò 'admin'.
                    role = "admin";
                }
                // Bước 3: Đã có vai trò, tiến hành đăng ký.
                proceedWithRegistration(displayName, username, email, password, role);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Nếu có lỗi khi kiểm tra, để an toàn, vẫn gán vai trò 'user'.
                proceedWithRegistration(displayName, username, email, password, "user");
            }
        });
    }

    /**
     * Hoàn tất quá trình đăng ký: tạo tài khoản trong Firebase Auth và lưu thông tin vào Realtime Database.
     * @param role Vai trò ('admin' hoặc 'user') đã được xác định ở bước trước.
     */
    private void proceedWithRegistration(String displayName, String username, String email, String password, String role) {
        // Tạo tài khoản trong Firebase Authentication bằng email và password.
        authManager.signUp(email, password, new FirebaseAuthManager.AuthListener() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser firebaseUser) {
                // Khi tài khoản Auth được tạo thành công, ta có được một UID duy nhất.
                // Bây giờ, lưu thông tin đầy đủ của user vào Realtime Database.
                User newUser = new User(firebaseUser.getUid(), username, displayName, email, role, 0);
                userRepository.createUser(newUser, () -> {
                    // Callback khi lưu vào database thành công.
                    String message = role.equals("admin") ? "Admin account created successfully!" : "Registration successful!";
                    Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                    // Chuyển người dùng đến màn hình đăng nhập.
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }, ex -> {
                    // Callback khi lưu vào database thất bại.
                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Lỗi khi tạo tài khoản trong Firebase Authentication (ví dụ: email sai định dạng, email đã tồn tại).
                Toast.makeText(RegisterActivity.this, "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
