package com.example.music_application.ui;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;
import com.example.music_application.data.Playlist;

import java.util.List;

public class PlaylistListActivity extends AppCompatActivity implements PlaylistAdapter.PlaylistListener {

    private DBHelper dbHelper;
    private PlaylistAdapter adapter;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_list);

        dbHelper = new DBHelper(this);
        userId = getIntent().getLongExtra("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Error: User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        RecyclerView rv = findViewById(R.id.rvPlaylists);
        Button btnCreate = findViewById(R.id.btnCreatePlaylist);

        adapter = new PlaylistAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        loadPlaylists();

        btnCreate.setOnClickListener(v -> showCreatePlaylistDialog());
    }

    private void loadPlaylists() {
        List<Playlist> playlists = dbHelper.getPlaylistsByUser(userId);
        adapter.setPlaylists(playlists);
    }

    private void showCreatePlaylistDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Playlist name");

        new AlertDialog.Builder(this)
                .setTitle("Create playlist")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        dbHelper.createPlaylist(userId, name);
                        loadPlaylists();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onPlaylistClick(Playlist playlist) {
        PlaylistDetailActivity.start(this, playlist.getId(), playlist.getName());
    }

    @Override
    public void onDeleteClick(Playlist playlist) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete '" + playlist.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deletePlaylist(playlist.getId());
                    loadPlaylists();
                    Toast.makeText(this, "'" + playlist.getName() + "' deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
