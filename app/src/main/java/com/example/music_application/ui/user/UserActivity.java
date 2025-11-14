package com.example.music_application.ui.user;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;

import java.util.List;

public class UserActivity extends AppCompatActivity implements UserSongAdapter.OnSongClickListener {

    private RecyclerView rvUserSongs;
    private TextView txtEmpty;
    private UserSongAdapter adapter;
    private MediaPlayer mediaPlayer;
    private List<Song> visibleSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        rvUserSongs = findViewById(R.id.rvUserSongs);
        txtEmpty = findViewById(R.id.txtEmpty);

        // chỉ lấy những bài admin đã "thêm"
        visibleSongs = SongRepository.getActiveSongs(this);

        if (visibleSongs.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            rvUserSongs.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            rvUserSongs.setVisibility(View.VISIBLE);

            adapter = new UserSongAdapter(visibleSongs, this);
            rvUserSongs.setLayoutManager(new LinearLayoutManager(this));
            rvUserSongs.setAdapter(adapter);
        }
    }

    @Override
    public void onSongClick(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = MediaPlayer.create(this, song.getResId());
        mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}