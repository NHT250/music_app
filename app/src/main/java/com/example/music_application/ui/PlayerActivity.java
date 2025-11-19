package com.example.music_application.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.service.MusicService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.music_application.model.MusicConstant.ACTION_PAUSE;
import static com.example.music_application.model.MusicConstant.ACTION_RESUME;
import static com.example.music_application.model.MusicConstant.ACTION_STOP;
import static com.example.music_application.model.MusicConstant.MUSIC_ACTION;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitle, txtArtist, txtCurrentTime, txtDuration, txtLikeCount;
    private ImageView imgCoverArt, btnPlayPause, btnNext, btnPrev, imgBack, btnLike, btnAddToPlaylist;
    private SeekBar seekBar;

    private MusicService musicService;
    private boolean isBound = false;
    private Song mSong;
    private boolean isLiked = false;
    private SongRepository songRepository;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarAction;
    private DatabaseReference likeRef;
    private ValueEventListener likeListener;

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
            updateUIFromService();
            startUpdatingSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        setupClickListeners();
        songRepository = new SongRepository();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("song")) {
            mSong = (Song) intent.getSerializableExtra("song");
            updateUI();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(musicReceiver, new IntentFilter(MUSIC_ACTION));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopUpdatingSeekBar();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        if (likeRef != null && likeListener != null) {
            likeRef.removeEventListener(likeListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(musicReceiver);
        if (likeRef != null && likeListener != null) {
            likeRef.removeEventListener(likeListener);
        }
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtDuration = findViewById(R.id.txtDuration);
        txtLikeCount = findViewById(R.id.txt_like_count_player);
        imgCoverArt = findViewById(R.id.imgCoverArt);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        imgBack = findViewById(R.id.imgBack);
        seekBar = findViewById(R.id.seekBar);
        btnLike = findViewById(R.id.btn_like_player);
        btnAddToPlaylist = findViewById(R.id.btn_add_to_playlist);
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (musicService != null && isBound) {
                if (musicService.isPlaying()) {
                    musicService.pauseMusic();
                } else {
                    musicService.resumeMusic();
                }
            }
        });

        imgBack.setOnClickListener(v -> finish());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && musicService != null && isBound) {
                    musicService.seekMusic(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdatingSeekBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startUpdatingSeekBar();
            }
        });

        btnNext.setOnClickListener(v -> Toast.makeText(this, "Next Clicked", Toast.LENGTH_SHORT).show());
        btnPrev.setOnClickListener(v -> Toast.makeText(this, "Previous Clicked", Toast.LENGTH_SHORT).show());
        btnLike.setOnClickListener(v -> handleLikeClick());
        btnAddToPlaylist.setOnClickListener(v -> {
            if (mSong != null) {
                Intent intent = new Intent(PlayerActivity.this, AddSongToPlaylistActivity.class);
                intent.putExtra("SONG_ID", mSong.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "No song is currently playing.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLikeClick() {
        if (mSong == null) return;
        songRepository.incrementLikes(mSong.getId(),
                () -> { /* UI is updated by listener */ },
                e -> Toast.makeText(PlayerActivity.this, "Failed to like song", Toast.LENGTH_SHORT).show());
    }

    private void setupLikeListener() {
        if (likeRef != null && likeListener != null) {
            likeRef.removeEventListener(likeListener);
        }
        if (mSong == null) return;

        likeRef = FirebaseDatabase.getInstance().getReference("songs").child(mSong.getId()).child("likes");
        likeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long likes = snapshot.getValue(Long.class);
                if (likes != null) {
                    mSong.setLikes(likes.intValue());
                    updateLikeUI();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // No-op
            }
        };
        likeRef.addValueEventListener(likeListener);
    }

    private void updateLikeUI() {
        if (mSong != null) {
            txtLikeCount.setText(String.valueOf(mSong.getLikes()));
            // You can add logic to check if the current user has liked the song and update the icon accordingly
            // For now, we just set it to filled if likes > 0
            btnLike.setImageResource(mSong.getLikes() > 0 ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
        }
    }

    private void handleMusicAction(int action) {
        switch (action) {
            case ACTION_PAUSE:
            case ACTION_RESUME:
                updateUIFromService();
                break;
            case ACTION_STOP:
                finish();
                break;
        }
    }

    private void updateUIFromService() {
        if (isBound && musicService != null) {
            mSong = musicService.getCurrentSong();
            updateUI();
        }
    }

    private void updateUI() {
        if (mSong != null) {
            txtTitle.setText(mSong.getTitle());
            txtArtist.setText(mSong.getArtist());
            
            if (mSong.getCover() != null && !mSong.getCover().isEmpty()) {
                Glide.with(this).load(mSong.getCover()).into(imgCoverArt);
            } else {
                imgCoverArt.setImageResource(R.drawable.ic_music);
            }
            
            updatePlayPauseButton();
            updateDuration();
            setupLikeListener(); 
            updateLikeUI();
        }
    }

    private void updatePlayPauseButton() {
        if (musicService != null && isBound) {
            btnPlayPause.setImageResource(musicService.isPlaying() ? R.drawable.ic_pause : R.drawable.ic_play_arrow);
        }
    }

    private void updateDuration() {
        if (musicService != null && isBound) {
            long duration = musicService.getDuration();
            if (duration > 0) {
                seekBar.setMax((int) duration);
                txtDuration.setText(formatDuration(duration));
            }
        }
    }

    private void startUpdatingSeekBar() {
        stopUpdatingSeekBar();
        updateSeekBarAction = new Runnable() {
            @Override
            public void run() {
                if (musicService != null && isBound && musicService.isPlaying()) {
                    long currentPosition = musicService.getCurrentPosition();
                    if (currentPosition >= 0) {
                        seekBar.setProgress((int) currentPosition);
                        txtCurrentTime.setText(formatDuration(currentPosition));
                    }
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(updateSeekBarAction);
    }

    private void stopUpdatingSeekBar() {
        if (updateSeekBarAction != null) {
            handler.removeCallbacks(updateSeekBarAction);
        }
    }

    private String formatDuration(long duration) {
        if (duration <= 0) return "00:00";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
