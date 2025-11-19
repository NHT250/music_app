package com.example.music_application.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.UserRepository;
import com.example.music_application.model.User;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private EditText edtDisplayName, edtUsername, edtPassword, edtRole;
    private TextView txtDisplayNameHeader, txtPlaylistCount;
    private Button btnSaveProfile;

    private FirebaseAuthManager authManager;
    private UserRepository userRepository;
    private User currentUser;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        authManager = new FirebaseAuthManager();
        userRepository = new UserRepository();
        firebaseUser = authManager.getCurrentUser();

        initViews();

        if (firebaseUser == null) {
            finish();
            return;
        }

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseUser != null) {
            loadUserProfile();
        }
    }

    private void initViews() {
        txtDisplayNameHeader = findViewById(R.id.txtDisplayNameHeader);
        edtDisplayName = findViewById(R.id.edtDisplayName);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtRole = findViewById(R.id.edtRole);
        txtPlaylistCount = findViewById(R.id.txtPlaylistCount);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
    }

    private void loadUserProfile() {
        userRepository.getUser(firebaseUser.getUid(), new UserRepository.UserListener() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
                populateUI(user);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(User user) {
        txtDisplayNameHeader.setText(user.getDisplayName());
        edtDisplayName.setText(user.getDisplayName());
        edtUsername.setText(user.getEmail());
        edtRole.setText(user.getRole());
        txtPlaylistCount.setText("Playlists: " + user.getPlaylistCount());
    }

    private void saveProfileChanges() {
        String newDisplayName = edtDisplayName.getText().toString().trim();
        String newPassword = edtPassword.getText().toString().trim();

        if (newDisplayName.isEmpty()) {
            Toast.makeText(this, "Display name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setDisplayName(newDisplayName);
        userRepository.updateUser(currentUser, 
            () -> Toast.makeText(ProfileActivity.this, "Display name updated.", Toast.LENGTH_SHORT).show(),
            e -> Toast.makeText(ProfileActivity.this, "Failed to update display name.", Toast.LENGTH_SHORT).show()
        );

        if (!newPassword.isEmpty()) {
            firebaseUser.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
