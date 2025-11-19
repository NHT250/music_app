package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.PlaylistListAdapter;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.PlaylistRepository;
import com.example.music_application.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistListActivity extends AppCompatActivity implements PlaylistListAdapter.OnPlaylistClickListener, PlaylistListAdapter.OnDeletePlaylistClickListener {

    private RecyclerView rvPlaylists;
    private Button btnCreatePlaylist;
    private PlaylistListAdapter adapter;
    private PlaylistRepository playlistRepository;
    private FirebaseAuthManager authManager;
    private List<Playlist> playlistList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_list);

        playlistRepository = new PlaylistRepository();
        authManager = new FirebaseAuthManager();

        initViews();
        setupRecyclerView();

        btnCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        loadPlaylists();
    }

    private void initViews() {
        rvPlaylists = findViewById(R.id.rvPlaylists);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
    }

    private void setupRecyclerView() {
        adapter = new PlaylistListAdapter(playlistList, this, this);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylists.setAdapter(adapter);
    }

    private void loadPlaylists() {
        String userId = authManager.getCurrentUser().getUid();
        if (userId != null) {
            playlistRepository.getUserPlaylists(userId, new PlaylistRepository.PlaylistListener() {
                @Override
                public void onPlaylistLoaded(List<Playlist> playlists) {
                    playlistList.clear();
                    playlistList.addAll(playlists);
                    adapter.setPlaylists(playlists);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(PlaylistListActivity.this, "Failed to load playlists.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Playlist");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistTitle = input.getText().toString().trim();
            if (!playlistTitle.isEmpty()) {
                createPlaylist(playlistTitle);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void createPlaylist(String title) {
        String userId = authManager.getCurrentUser().getUid();
        Playlist newPlaylist = new Playlist(null, title, userId, new ArrayList<>());

        playlistRepository.createPlaylist(newPlaylist, 
            () -> {
                Toast.makeText(this, "Playlist created.", Toast.LENGTH_SHORT).show();
                loadPlaylists();
            },
            e -> Toast.makeText(this, "Failed to create playlist.", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        Intent intent = new Intent(this, PlaylistDetailActivity.class);
        intent.putExtra("playlistId", playlist.getId());
        intent.putExtra("playlistTitle", playlist.getTitle());
        startActivity(intent);
    }

    @Override
    public void onDeletePlaylistClick(Playlist playlist) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete this playlist?")
            .setPositiveButton("Delete", (dialog, which) -> deletePlaylist(playlist))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deletePlaylist(Playlist playlist) {
        String userId = authManager.getCurrentUser().getUid();
        if (userId != null) {
            playlistRepository.deletePlaylist(userId, playlist.getId(),
                () -> {
                    Toast.makeText(this, "Playlist deleted.", Toast.LENGTH_SHORT).show();
                    loadPlaylists(); // Refresh the list
                },
                e -> Toast.makeText(this, "Failed to delete playlist.", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
