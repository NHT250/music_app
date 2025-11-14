package com.example.music_application.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;
import com.example.music_application.data.User;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private DBHelper dbHelper;

    private static final String PREF_AUTH = "museflow_auth";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView txtGoRegister = findViewById(R.id.txtGoRegister);

        btnLogin.setOnClickListener(v -> doLogin());

        txtGoRegister.setOnClickListener(v -> {
            Intent i = new Intent(this, RegisterActivity.class);
            startActivity(i);
        });
    }

    private void doLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.login(username, password);
        if (user == null) {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu trạng thái đăng nhập
        SharedPreferences prefs = getSharedPreferences(PREF_AUTH, MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putLong(KEY_USER_ID, user.getId())
                .putString(KEY_ROLE, user.getRole())
                .apply();

        // Điều hướng theo role
        if ("admin".equalsIgnoreCase(user.getRole())) {
            Intent i = new Intent(this, AdminDashboardActivity.class);
            i.putExtra("user_id", user.getId());
            startActivity(i);
        } else {
            Intent i = new Intent(this, UserHomeActivity.class);
            i.putExtra("user_id", user.getId());
            startActivity(i);
        }

        finish();
    }
}