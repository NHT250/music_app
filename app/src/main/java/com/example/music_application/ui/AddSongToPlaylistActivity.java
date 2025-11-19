package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.PlaylistSelectionAdapter;
import com.example.music_application.adapter.SongSelectionAdapter;
import com.example.music_application.firebase.PlaylistRepository;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Playlist;
import com.example.music_application.model.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddSongToPlaylistActivity extends AppCompatActivity {

    private RecyclerView rvSelection;
    private FloatingActionButton fabSaveChanges;
    private SongSelectionAdapter songSelectionAdapter;
    private PlaylistSelectionAdapter playlistSelectionAdapter;
    private SongRepository songRepository;
    private PlaylistRepository playlistRepository;

    private String playlistId;
    private String userId;
    private String songIdToAdd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song_to_playlist);

        songRepository = new SongRepository();
        playlistRepository = new PlaylistRepository();

        rvSelection = findViewById(R.id.rvSongSelection);
        fabSaveChanges = findViewById(R.id.fabSaveChanges);
        rvSelection.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (intent.hasExtra("SONG_ID")) {
            songIdToAdd = intent.getStringExtra("SONG_ID");
            fabSaveChanges.setVisibility(View.GONE);
            loadPlaylistsForSong();
        } else {
            playlistId = intent.getStringExtra("PLAYLIST_ID");
            ArrayList<String> currentSongIds = intent.getStringArrayListExtra("CURRENT_SONG_IDS");
            loadAllSongs(currentSongIds);
            fabSaveChanges.setOnClickListener(v -> saveChanges());
        }
    }

    private void loadPlaylistsForSong() {
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        playlistRepository.getUserPlaylists(userId, new PlaylistRepository.PlaylistListener() {
            @Override
            public void onPlaylistLoaded(List<Playlist> playlists) {
                playlistSelectionAdapter = new PlaylistSelectionAdapter(playlists, playlist -> addSongToPlaylist(playlist.getId()));
                rvSelection.setAdapter(playlistSelectionAdapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddSongToPlaylistActivity.this, "Failed to load playlists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSongToPlaylist(String selectedPlaylistId) {
        if (userId == null || songIdToAdd == null) return;
        playlistRepository.addSongsToPlaylist(userId, selectedPlaylistId, Collections.singletonList(songIdToAdd),
                () -> {
                    Toast.makeText(this, "Song added to playlist!", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> Toast.makeText(this, "Failed to add song to playlist", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadAllSongs(ArrayList<String> currentSongIds) {
        songRepository.getAllSongs(new SongRepository.SongListListener() {
            @Override
            public void onSongListLoaded(List<Song> songs) {
                songSelectionAdapter = new SongSelectionAdapter(songs, currentSongIds);
                rvSelection.setAdapter(songSelectionAdapter);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(AddSongToPlaylistActivity.this, "Failed to load songs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        if (songSelectionAdapter != null) {
            List<String> newSongIds = songSelectionAdapter.getSelectedSongIds();
            playlistRepository.addSongsToPlaylist(userId, playlistId, newSongIds,
                    () -> {
                        Toast.makeText(this, "Playlist updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> Toast.makeText(this, "Failed to update playlist", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
