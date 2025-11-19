package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.PlaylistSongsAdapter;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.model.Playlist;
import com.example.music_application.model.Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity implements PlaylistSongsAdapter.OnSongClickListener, PlaylistSongsAdapter.OnDeleteClickListener {

    private TextView txtPlaylistTitle;
    private RecyclerView rvPlaylistSongs;
    private Button btnAddSongs, btnShuffle;

    private PlaylistSongsAdapter adapter;
    private FirebaseAuthManager authManager;

    private String playlistId;
    private String playlistTitle;
    private Playlist currentPlaylist;
    private List<Song> songList = new ArrayList<>();

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylistSongs();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        authManager = new FirebaseAuthManager();

        Intent intent = getIntent();
        playlistId = intent.getStringExtra("playlistId");
        playlistTitle = intent.getStringExtra("playlistTitle");

        initViews();
        setupRecyclerView();

        txtPlaylistTitle.setText(playlistTitle);
        btnAddSongs.setOnClickListener(v -> {
            Intent addSongsIntent = new Intent(PlaylistDetailActivity.this, AddSongToPlaylistActivity.class);
            addSongsIntent.putExtra("PLAYLIST_ID", playlistId);
            addSongsIntent.putExtra("USER_ID", authManager.getCurrentUser().getUid());
            if (currentPlaylist != null && currentPlaylist.getSongIds() != null) {
                addSongsIntent.putStringArrayListExtra("CURRENT_SONG_IDS", new ArrayList<>(currentPlaylist.getSongIds()));
            }
            startActivity(addSongsIntent);
        });

        btnShuffle.setOnClickListener(v -> shufflePlay());
    }

    private void initViews() {
        txtPlaylistTitle = findViewById(R.id.txtPlaylistTitle);
        rvPlaylistSongs = findViewById(R.id.rvPlaylistSongs);
        btnAddSongs = findViewById(R.id.btnAddSongsToPlaylist);
        btnShuffle = findViewById(R.id.btnShuffle);
    }

    private void setupRecyclerView() {
        adapter = new PlaylistSongsAdapter(songList, this, this);
        rvPlaylistSongs.setLayoutManager(new LinearLayoutManager(this));
        rvPlaylistSongs.setAdapter(adapter);
    }

    private void loadPlaylistSongs() {
        String userId = authManager.getCurrentUser().getUid();
        if (userId == null) return;

        FirebaseDatabase.getInstance().getReference("playlists").child(userId).child(playlistId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentPlaylist = snapshot.getValue(Playlist.class);
                    if (currentPlaylist != null && currentPlaylist.getSongIds() != null && !currentPlaylist.getSongIds().isEmpty()) {
                        fetchSongsFromIds(currentPlaylist.getSongIds());
                    } else {
                        songList.clear();
                        adapter.setSongs(songList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                     Toast.makeText(PlaylistDetailActivity.this, "Failed to load playlist details.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchSongsFromIds(List<String> songIds) {
        songList.clear();
        DatabaseReference songsRef = FirebaseDatabase.getInstance().getReference("songs");
        
        if (songIds.isEmpty()) {
            adapter.setSongs(new ArrayList<>());
            return;
        }

        for (String songId : songIds) {
            songsRef.child(songId).addListenerForSingleValueEvent(new ValueEventListener() {
                 @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Song song = snapshot.getValue(Song.class);
                        if (song != null) {
                           songList.add(song);
                        }
                    }
                    if(songList.size() == songIds.size()){
                        adapter.setSongs(songList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    public void onDeleteClick(Song song) {
        String userId = authManager.getCurrentUser().getUid();
        if (userId == null || currentPlaylist == null || currentPlaylist.getSongIds() == null) {
            return;
        }

        DatabaseReference playlistRef = FirebaseDatabase.getInstance()
                .getReference("playlists")
                .child(userId)
                .child(playlistId);

        List<String> updatedSongIds = new ArrayList<>(currentPlaylist.getSongIds());
        updatedSongIds.remove(song.getId());

        playlistRef.child("songIds").setValue(updatedSongIds)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PlaylistDetailActivity.this, "Song removed from playlist", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(PlaylistDetailActivity.this, "Failed to remove song", Toast.LENGTH_SHORT).show();
                });
    }

    private void shufflePlay() {
        if (songList.isEmpty()) {
            Toast.makeText(this, "Playlist is empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<Song> shuffledList = new ArrayList<>(songList);
        Collections.shuffle(shuffledList);
        onSongClick(shuffledList.get(0));
    }

    @Override
    public void onSongClick(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("SONG_LIST", (Serializable) songList);
        intent.putExtra("CURRENT_SONG_POSITION", songList.indexOf(song));
        startActivity(intent);
    }
}
