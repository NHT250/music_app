package com.example.music_application.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.player.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlaylistDetailActivity extends AppCompatActivity implements OnSongActionClickListener {

    private static final String EXTRA_PLAYLIST_ID = "extra_playlist_id";
    private static final String EXTRA_PLAYLIST_NAME = "extra_playlist_name";

    public static void start(Context ctx, long id, String name) {
        Intent i = new Intent(ctx, PlaylistDetailActivity.class);
        i.putExtra(EXTRA_PLAYLIST_ID, id);
        i.putExtra(EXTRA_PLAYLIST_NAME, name);
        ctx.startActivity(i);
    }

    private DBHelper dbHelper;
    private SongAdapter adapter;
    private String playlistName;
    private long playlistId;

    private List<Song> songsInPlaylist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        TextView txtTitle = findViewById(R.id.txtPlaylistTitle);
        RecyclerView rv = findViewById(R.id.rvPlaylistSongs);
        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnAddSongs = findViewById(R.id.btnAddSongsToPlaylist);

        dbHelper = new DBHelper(this);

        Intent i = getIntent();
        playlistId = i.getLongExtra(EXTRA_PLAYLIST_ID, -1);
        playlistName = i.getStringExtra(EXTRA_PLAYLIST_NAME);
        txtTitle.setText(playlistName);

        if (playlistId == -1) {
            Toast.makeText(this, "Error: Playlist not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new SongAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnAddSongs.setOnClickListener(v -> showAddSongsDialog());
        btnShuffle.setOnClickListener(v -> shufflePlay());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSongs();
    }

    private void loadSongs() {
        List<com.example.music_application.data.Song> dataSongs = dbHelper.getSongsByPlaylist(playlistId);
        songsInPlaylist.clear();
        for (com.example.music_application.data.Song dataSong : dataSongs) {
            songsInPlaylist.add(new Song(dataSong.getTitle(), dataSong.getResId(), dataSong.isFavorite(), dataSong.getCategory()));
        }
        adapter.setSongs(songsInPlaylist);
    }

    private void showAddSongsDialog() {
        // 1. Lấy tất cả bài hát mà user thấy trên homepage
        List<Song> allActiveSongs = SongRepository.getActiveSongs(this);

        // 2. Lấy danh sách ID của các bài đã có trong playlist này
        List<Integer> resIdsInPlaylist = songsInPlaylist.stream().map(Song::getResId).collect(Collectors.toList());

        // 3. Lọc ra những bài chưa có trong playlist
        List<Song> availableSongs = allActiveSongs.stream()
                .filter(song -> !resIdsInPlaylist.contains(song.getResId()))
                .collect(Collectors.toList());

        if (availableSongs.isEmpty()) {
            Toast.makeText(this, "All available songs are already in this playlist.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Hiển thị dialog để chọn
        String[] songTitles = availableSongs.stream().map(Song::getTitle).toArray(String[]::new);

        new AlertDialog.Builder(this)
                .setTitle("Add song to '" + playlistName + "'")
                .setItems(songTitles, (dialog, which) -> {
                    Song chosenSong = availableSongs.get(which);

                    // Cần tìm db ID từ res ID
                    com.example.music_application.data.Song songFromDb = dbHelper.getSongByResId(chosenSong.getResId());
                    if (songFromDb != null) {
                        dbHelper.addSongToPlaylist(playlistId, songFromDb.getId());
                        loadSongs(); // Tải lại danh sách
                        Toast.makeText(this, "'" + chosenSong.getTitle() + "' added.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: Could not find song in database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void shufflePlay() {
        if (songsInPlaylist.isEmpty()) {
            Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        Collections.shuffle(songsInPlaylist);
        Song firstSong = songsInPlaylist.get(0);
        // ... (logic phát nhạc)
    }

    @Override
    public void onSongClick(Song song) {
        // ... (logic phát nhạc)
    }

    @Override
    public void onFavoriteClick(Song song) {}

    @Override
    public void onAddToPlaylistClick(Song song) {}
}