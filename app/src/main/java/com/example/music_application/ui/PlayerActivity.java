package com.example.music_application.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.model.Song;
import com.example.music_application.player.MusicService;
import com.example.music_application.player.PlayerManager;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitle, txtArtist, txtCurrentTime, txtDuration, txtLikeCount;
    private ImageView imgCoverArt, btnPlayPause, btnNext, btnPrev, imgBack, btnLike, btnAddToPlaylist;
    private SeekBar seekBar;

    private boolean isLiked = false; // Trạng thái like tạm thời

    private MediaController mediaController;
    private ListenableFuture<MediaController> controllerFuture;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateSeekBarAction;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            updatePlayPauseButton(isPlaying);
        }

        @Override
        public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
            updateUiForMediaItem(mediaItem);
        }

        @Override
        public void onTimelineChanged(@NonNull Timeline timeline, int reason) {
            if (timeline.getWindowCount() > 0) {
                updateDuration();
            }
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_READY) {
                updateDuration();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeMediaController();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaController != null) {
            mediaController.removeListener(playerListener);
        }
        MediaController.releaseFuture(controllerFuture);
        stopUpdatingSeekBar();
    }

    private void initializeMediaController() {
        SessionToken sessionToken = new SessionToken(this, new ComponentName(this, MusicService.class));
        controllerFuture = new MediaController.Builder(this, sessionToken).buildAsync();

        controllerFuture.addListener(() -> {
            try {
                mediaController = controllerFuture.get();
                mediaController.addListener(playerListener);
                prepareAndSetPlaylist();
                startUpdatingSeekBar();
            } catch (Exception e) {
                Toast.makeText(this, "Error connecting to media service", Toast.LENGTH_SHORT).show();
            }
        }, MoreExecutors.directExecutor());
    }

    private void prepareAndSetPlaylist() {
        List<Song> songList = PlayerManager.getInstance().getSongList();
        if (songList == null || songList.isEmpty()) {
            Toast.makeText(this, "Song list is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songList) {
            MediaMetadata metadata = new MediaMetadata.Builder()
                    .setTitle(song.getTitle())
                    .setArtist(song.getArtist())
                    .setArtworkUri(song.getCover() != null ? Uri.parse(song.getCover()) : null)
                    .build();
            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUrl())
                    .setMediaId(song.getId())
                    .setMediaMetadata(metadata)
                    .build();
            mediaItems.add(mediaItem);
        }

        mediaController.setMediaItems(mediaItems, false);
        mediaController.prepare();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("song_position")) {
            int songPosition = intent.getIntExtra("song_position", -1);
            if (songPosition != -1) {
                mediaController.seekTo(songPosition, C.TIME_UNSET);
                mediaController.play();
            }
            setIntent(null);
        }
    }

    private void initViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);
        txtCurrentTime = findViewById(R.id.txtCurrentTime);
        txtDuration = findViewById(R.id.txtDuration);
        imgCoverArt = findViewById(R.id.imgCoverArt);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        imgBack = findViewById(R.id.imgBack);
        seekBar = findViewById(R.id.seekBar);
        btnLike = findViewById(R.id.btn_like_player);
        btnAddToPlaylist = findViewById(R.id.btn_add_to_playlist);
        txtLikeCount = findViewById(R.id.txt_like_count_player);
    }

    private void setupClickListeners() {
        btnPlayPause.setOnClickListener(v -> {
            if (mediaController == null) return;
            if (mediaController.isPlaying()) {
                mediaController.pause();
            } else {
                mediaController.play();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (mediaController != null && mediaController.hasNextMediaItem()) {
                mediaController.seekToNext();
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (mediaController != null && mediaController.hasPreviousMediaItem()) {
                mediaController.seekToPrevious();
            }
        });

        imgBack.setOnClickListener(v -> finish());

        btnLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            btnLike.setImageResource(isLiked ? android.R.drawable.star_on : android.R.drawable.star_off);
            Toast.makeText(this, isLiked ? "Liked!" : "Unliked!", Toast.LENGTH_SHORT).show();
        });

        btnAddToPlaylist.setOnClickListener(v -> {
            Toast.makeText(this, "Add to playlist clicked!", Toast.LENGTH_SHORT).show();
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int userProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    userProgress = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdatingSeekBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaController != null) {
                    mediaController.seekTo(userProgress);
                }
                startUpdatingSeekBar();
            }
        });
    }

    private void updateUiForMediaItem(@Nullable MediaItem mediaItem) {
        if (mediaItem == null) return;
        MediaMetadata metadata = mediaItem.mediaMetadata;
        txtTitle.setText(metadata.title);
        txtArtist.setText(metadata.artist);
        Glide.with(this).load(metadata.artworkUri).placeholder(R.drawable.ic_music_note).into(imgCoverArt);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        btnPlayPause.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
    }

    private void updateDuration() {
        if (mediaController == null) return;
        long duration = mediaController.getDuration();
        if (duration > 0 && duration != C.TIME_UNSET) {
            seekBar.setMax((int) duration);
            txtDuration.setText(formatDuration(duration));
        }
    }

    private void startUpdatingSeekBar() {
        stopUpdatingSeekBar();
        updateSeekBarAction = new Runnable() {
            @Override
            public void run() {
                if (mediaController != null) {
                    long currentPosition = mediaController.getCurrentPosition();
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
        if (duration < 0) return "00:00";
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
