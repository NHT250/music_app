package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtDisplayName, edtUsername, edtPassword, edtConfirmPassword;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DBHelper(this);

        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView txtGoLogin = findViewById(R.id.txtGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        txtGoLogin.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void doRegister() {
        String displayName = edtDisplayName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirm = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(displayName) ||
                TextUtils.isEmpty(username) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.registerUser(username, password, displayName);
        if (result == -1) {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        } else if (result > 0) {
            Toast.makeText(this, "Register success! Please login.", Toast.LENGTH_SHORT).show();
            // quay về màn Login
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Register failed", Toast.LENGTH_SHORT).show();
        }
    }
}