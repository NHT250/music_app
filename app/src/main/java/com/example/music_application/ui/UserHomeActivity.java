package com.example.music_application.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.adapter.RecommendedAdapter;
import com.example.music_application.adapter.TopHitsAdapter;
import com.example.music_application.firebase.FirebaseAuthManager;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.service.MusicService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.music_application.model.MusicConstant.ACTION_PAUSE;
import static com.example.music_application.model.MusicConstant.ACTION_RESUME;
import static com.example.music_application.model.MusicConstant.ACTION_STOP;
import static com.example.music_application.model.MusicConstant.MUSIC_ACTION;

public class UserHomeActivity extends AppCompatActivity {
    private FrameLayout miniPlayerContainer;
    private ImageView imageSong, btnClose, btnProfile, btnSettings, btnLogout, btnPlaylistList;
    private TextView textSongName, textArtistName;
    private ImageButton buttonPlayPause;
    private RecyclerView rvTopHits, rvRecommended;
    private ChipGroup chipGroupCategories;

    private MusicService musicService;
    private boolean isBound = false;
    private Song mSong;
    private FirebaseAuthManager authManager;
    private SongRepository songRepository;

    private TopHitsAdapter topHitsAdapter;
    private RecommendedAdapter recommendedAdapter;
    private List<Song> allSongs = new ArrayList<>();
    private List<Song> topHitsSongs = new ArrayList<>();
    private List<Song> recommendedSongs = new ArrayList<>();

    private BroadcastReceiver musicReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(MUSIC_ACTION, 0);
            handleMusicAction(action);
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            isBound = true;
            updateMiniPlayerState();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        authManager = new FirebaseAuthManager();
        songRepository = new SongRepository();

        initViews();
        setupRecyclerViews();
        setupClickListeners();
        setupCategoryChips();
        loadSongs();

        LocalBroadcastManager.getInstance(this).registerReceiver(musicReceiver, new IntentFilter(MUSIC_ACTION));
        bindService(new Intent(this, MusicService.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateMiniPlayerState();
    }

    private void initViews() {
        miniPlayerContainer = findViewById(R.id.mini_player_container);
        imageSong = findViewById(R.id.image_song);
        textSongName = findViewById(R.id.text_song_name);
        textArtistName = findViewById(R.id.text_artist_name);
        buttonPlayPause = findViewById(R.id.button_play_pause);
        btnClose = findViewById(R.id.button_close);
        rvTopHits = findViewById(R.id.rv_top_hits);
        rvRecommended = findViewById(R.id.rv_recommended);
        btnProfile = findViewById(R.id.btn_profile);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);
        btnPlaylistList = findViewById(R.id.btn_playlist_list);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
    }

    private void setupRecyclerViews() {
        topHitsAdapter = new TopHitsAdapter(this, topHitsSongs, this::onSongSelected);
        rvTopHits.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopHits.setAdapter(topHitsAdapter);

        recommendedAdapter = new RecommendedAdapter(this, recommendedSongs, this::onSongSelected);
        rvRecommended.setLayoutManager(new LinearLayoutManager(this));
        rvRecommended.setAdapter(recommendedAdapter);
    }

    private void loadSongs() {
        songRepository.getAllSongs(new SongRepository.SongListListener() {
            @Override
            public void onSongListLoaded(List<Song> songs) {
                // Sort songs by likes in descending order
                Collections.sort(songs, (s1, s2) -> Integer.compare(s2.getLikes(), s1.getLikes()));

                allSongs.clear();
                allSongs.addAll(songs);
                
                topHitsSongs.clear();
                topHitsSongs.addAll(songs.subList(0, Math.min(5, songs.size())));
                topHitsAdapter.notifyDataSetChanged();

                filterSongs("All");
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(UserHomeActivity.this, "Failed to load songs: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupCategoryChips() {
        String[] categories = {"All", "Pop", "Indie", "Hip-Hop", "Ballad"};
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chipGroupCategories.clearCheck();
                chip.setChecked(true);
                filterSongs(category);
            });
            chipGroupCategories.addView(chip);
        }
        ((Chip)chipGroupCategories.getChildAt(0)).setChecked(true);
    }

    private void filterSongs(String category) {
        recommendedSongs.clear();
        if (category.equals("All")) {
            recommendedSongs.addAll(allSongs);
        } else {
            for (Song song : allSongs) {
                if (song.getCategory() != null && song.getCategory().equalsIgnoreCase(category)) {
                    recommendedSongs.add(song);
                }
            }
        }
        recommendedAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, ProfileActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, SettingsActivity.class)));
        btnPlaylistList.setOnClickListener(v -> startActivity(new Intent(UserHomeActivity.this, PlaylistListActivity.class)));

        btnLogout.setOnClickListener(v -> {
            authManager.signOut();
            startActivity(new Intent(UserHomeActivity.this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        });

        buttonPlayPause.setOnClickListener(v -> {
            if (musicService != null && isBound) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                } else {
                    musicService.resumeMusic();
                }
            }
        });

        btnClose.setOnClickListener(v -> {
            if (musicService != null && isBound) {
                musicService.stopMusic();
            }
        });

        miniPlayerContainer.setOnClickListener(v -> {
            if (mSong != null) {
                Intent playerIntent = new Intent(this, PlayerActivity.class);
                playerIntent.putExtra("song", mSong);
                startActivity(playerIntent);
            }
        });
    }

    private void onSongSelected(Song selectedSong) {
        if (selectedSong == null) return;
        mSong = selectedSong;

        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.putExtra("song", (Serializable) selectedSong);
        startService(serviceIntent);

        Intent playerIntent = new Intent(this, PlayerActivity.class);
        playerIntent.putExtra("song", (Serializable) selectedSong);
        startActivity(playerIntent);
    }

    private void handleMusicAction(int action) {
        switch (action) {
            case ACTION_PAUSE:
            case ACTION_RESUME:
                updateMiniPlayerState();
                break;
            case ACTION_STOP:
                hideMiniPlayer();
                break;
        }
    }

    private void updateMiniPlayerState() {
        if (isBound && musicService != null && musicService.getCurrentSong() != null) {
            mSong = musicService.getCurrentSong();
            miniPlayerContainer.setVisibility(View.VISIBLE);
            textSongName.setText(mSong.getTitle());
            textArtistName.setText(mSong.getArtist());
            buttonPlayPause.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play);

            if (mSong.getCover() != null && !mSong.getCover().isEmpty()) {
                Glide.with(this).load(mSong.getCover()).into(imageSong);
            } else {
                imageSong.setImageResource(R.drawable.ic_music);
            }
        } else {
            hideMiniPlayer();
        }
    }

    private void hideMiniPlayer() {
        miniPlayerContainer.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicReceiver);
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }
}
