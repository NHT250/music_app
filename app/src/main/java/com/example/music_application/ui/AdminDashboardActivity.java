package com.example.music_application.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.ui.admin.AdminActivity;
import com.example.music_application.ui.admin.AdminSongAdapter;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements AdminSongAdapter.OnSongActionListener {

    private RecyclerView recyclerView;
    private AdminSongAdapter adapter;
    private long adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminId = getIntent().getLongExtra("user_id", -1);

        recyclerView = findViewById(R.id.rvAdminSongs);
        ImageView btnProfile = findViewById(R.id.btnProfile);
        ImageView btnAdminLogout = findViewById(R.id.btnAdminLogout);
        ImageView btnManageSongs = findViewById(R.id.btnManageSongs);

        List<Song> activeSongs = SongRepository.getActiveSongs(this);

        adapter = new AdminSongAdapter(activeSongs, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminProfileActivity.class);
            i.putExtra("user_id", adminId);
            startActivity(i);
        });

        btnAdminLogout.setOnClickListener(v -> logout());

        btnManageSongs.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<Song> activeSongs = SongRepository.getActiveSongs(this);
        adapter.setSongs(activeSongs);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("museflow_auth", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void onEdit(Song song) {
        // Delegate to AdminActivity
        Toast.makeText(this, "Please use the 'Manage Songs' screen to edit.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDelete(Song song) {
        // Delegate to AdminActivity
        Toast.makeText(this, "Please use the 'Manage Songs' screen to delete.", Toast.LENGTH_SHORT).show();
    }
}