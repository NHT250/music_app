package com.example.music_application.ui;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;
import com.example.music_application.data.User;

public class UserProfileActivity extends AppCompatActivity {

    private static final String PREF_PROFILE = "museflow_profile";

    private EditText edtDisplayName, edtUsername, edtPassword, edtRole;
    private TextView txtDisplayNameHeader, txtPlaylistCount;
    private ImageView imgAvatar;
    private DBHelper dbHelper;
    private long userId;
    private Uri avatarUri;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);

        imgAvatar = findViewById(R.id.imgAvatar);
        txtDisplayNameHeader = findViewById(R.id.txtDisplayNameHeader);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtRole = findViewById(R.id.edtRole);
        txtPlaylistCount = findViewById(R.id.txtPlaylistCount);
        Button btnSave = findViewById(R.id.btnSaveProfile);

        userId = getIntent().getLongExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupImagePicker();
        loadUserProfile();
        loadAvatar();
        loadPlaylistCount();

        imgAvatar.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        avatarUri = uri;
                        imgAvatar.setImageURI(uri);
                        saveAvatarUri(uri);
                    }
                }
        );
    }

    private void loadUserProfile() {
        User user = dbHelper.getUserById(userId);
        if (user != null) {
            edtDisplayName.setText(user.getDisplayName());
            txtDisplayNameHeader.setText(user.getDisplayName());
            edtUsername.setText(user.getUsername());
            edtPassword.setText(user.getPassword());
            edtRole.setText(user.getRole());
        }
    }

    private void loadPlaylistCount() {
        int count = dbHelper.getPlaylistCountForUser(userId);
        txtPlaylistCount.setText("My playlists: " + count);
    }

    private void loadAvatar() {
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, MODE_PRIVATE);
        String key = "avatar_user_" + userId;
        String uriStr = prefs.getString(key, null);
        if (uriStr != null) {
            avatarUri = Uri.parse(uriStr);
            imgAvatar.setImageURI(avatarUri);
        }
    }

    private void saveAvatarUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences(PREF_PROFILE, MODE_PRIVATE);
        String key = "avatar_user_" + userId;
        prefs.edit().putString(key, uri.toString()).apply();
    }

    private void saveProfile() {
        String displayName = edtDisplayName.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (displayName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int rows = dbHelper.updateUser(userId, username, password, displayName);
        if (rows > 0) {
            txtDisplayNameHeader.setText(displayName);
            Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show();
        }
    }
}
