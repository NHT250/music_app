package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.AdminSongAdapter;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity implements AdminSongAdapter.OnSongListener {

    private RecyclerView recyclerView;
    private AdminSongAdapter adapter;
    private SongRepository songRepository;
    private FirebaseAuthManager authManager;
    private List<Song> songList = new ArrayList<>();
    private ImageView btnSettings, btnProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        songRepository = new SongRepository();
        authManager = new FirebaseAuthManager();

        recyclerView = findViewById(R.id.recyclerViewSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSongAdapter(songList, this);
        recyclerView.setAdapter(adapter);

        btnSettings = findViewById(R.id.btnSettings);
        btnProfile = findViewById(R.id.btnProfile);

        FloatingActionButton fabAddSong = findViewById(R.id.fabAddSong);
        fabAddSong.setOnClickListener(view -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminEditSongActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            authManager.signOut();
            startActivity(new Intent(AdminHomeActivity.this, LoginActivity.class));
            finish();
        });
        
        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, SettingsActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, ProfileActivity.class));
        });

        loadSongs();
    }

    private void loadSongs() {
        songRepository.getAllSongs(new SongRepository.SongListListener() {
            @Override
            public void onSongListLoaded(List<Song> songs) {
                songList.clear();
                songList.addAll(songs);
                adapter.setSongs(songs);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AdminHomeActivity.this, "Failed to load songs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditClick(Song song) {
        Intent intent = new Intent(AdminHomeActivity.this, AdminEditSongActivity.class);
        intent.putExtra("song", song);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Song song) {
        songRepository.deleteSong(song.getId(),
                () -> {
                    Toast.makeText(AdminHomeActivity.this, "Song deleted", Toast.LENGTH_SHORT).show();
                    loadSongs(); // Reload the list
                },
                e -> Toast.makeText(AdminHomeActivity.this, "Failed to delete song: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSongs();
    }
}
