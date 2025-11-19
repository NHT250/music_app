package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.SongSelectionAdapter;
import com.example.music_application.firebase.PlaylistRepository;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AddSongToPlaylistActivity extends AppCompatActivity {

    private RecyclerView rvSongSelection;
    private FloatingActionButton fabSaveChanges;
    private SongSelectionAdapter adapter;
    private SongRepository songRepository;
    private PlaylistRepository playlistRepository;

    private String playlistId;
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song_to_playlist);

        songRepository = new SongRepository();
        playlistRepository = new PlaylistRepository();

        rvSongSelection = findViewById(R.id.rvSongSelection);
        fabSaveChanges = findViewById(R.id.fabSaveChanges);
        rvSongSelection.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        playlistId = intent.getStringExtra("PLAYLIST_ID");
        userId = intent.getStringExtra("USER_ID");
        ArrayList<String> currentSongIds = intent.getStringArrayListExtra("CURRENT_SONG_IDS");

        loadAllSongs(currentSongIds);

        fabSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadAllSongs(ArrayList<String> currentSongIds) {
        songRepository.getAllSongs(new SongRepository.SongListListener() {
            @Override
            public void onSongListLoaded(List<Song> songs) {
                adapter = new SongSelectionAdapter(songs, currentSongIds);
                rvSongSelection.setAdapter(adapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddSongToPlaylistActivity.this, "Failed to load songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        if (adapter != null) {
            List<String> newSongIds = adapter.getSelectedSongIds();
            playlistRepository.addSongsToPlaylist(userId, playlistId, newSongIds,
                    () -> {
                        Toast.makeText(this, "Playlist updated!", Toast.LENGTH_SHORT).show();
                        finish(); // Quay lại màn hình chi tiết playlist
                    },
                    e -> Toast.makeText(this, "Failed to update playlist", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
