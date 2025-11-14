package com.example.music_application.player;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music_application.R;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.ui.PlayerActivity;

import java.io.IOException;
import java.util.List;

public class MusicService extends Service {

    public static final String ACTION_START = "com.example.music_application.ACTION_START";
    public static final String ACTION_PLAY_PAUSE = "com.example.music_application.ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "com.example.music_application.ACTION_NEXT";
    public static final String ACTION_PREV = "com.example.music_application.ACTION_PREV";
    public static final String ACTION_STOP = "com.example.music_application.ACTION_STOP";

    // Broadcast để UI lắng nghe
    public static final String ACTION_STATE_CHANGED = "com.example.music_application.ACTION_STATE_CHANGED";

    public static final String EXTRA_SONG_ID = "extra_song_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_ARTIST = "extra_artist";
    public static final String EXTRA_FILE_PATH = "extra_file_path";
    public static final String EXTRA_IS_PLAYING = "extra_is_playing";
    public static final String EXTRA_QUEUE = "extra_queue";
    public static final String EXTRA_QUEUE_INDEX = "extra_queue_idx";

    private static final String CHANNEL_ID = "music_application_player_channel";
    private static final int NOTIFICATION_ID = 1;

    // KEY settings giống trong SettingsActivity
    private static final String PREF_NAME = "museflow_prefs";
    private static final String KEY_AUTO_NEXT = "pref_auto_next";

    private MediaPlayer mediaPlayer;
    private int currentSongResId = -1;
    private String currentTitle = "";
    private String currentArtist = "";
    private boolean isPlaying = false;

    // Queue phát nhạc (dùng cho playlist shuffle)
    private int[] playQueue = null;
    private int queueIndex = -1;
    private boolean useQueue = false;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getAction();

        if (action == null || ACTION_START.equals(action)) {
            handleStart(intent);
        } else {
            switch (action) {
                case ACTION_PLAY_PAUSE:
                    handlePlayPause();
                    break;
                case ACTION_NEXT:
                    handleNext();
                    break;
                case ACTION_PREV:
                    handlePrev();
                    break;
                case ACTION_STOP:
                    stopSelf();
                    break;
            }
        }

        return START_STICKY;
    }

    private void handleStart(Intent intent) {
        useQueue = false;
        playQueue = null;
        queueIndex = -1;

        int songResIdFromIntent = intent.getIntExtra(EXTRA_SONG_ID, -1);

        int[] queue = intent.getIntArrayExtra(EXTRA_QUEUE);
        int idxFromIntent = intent.getIntExtra(EXTRA_QUEUE_INDEX, 0);

        if (queue != null && queue.length > 0) {
            playQueue = queue;
            queueIndex = idxFromIntent;
            if (queueIndex < 0 || queueIndex >= playQueue.length) {
                queueIndex = 0;
            }
            useQueue = true;

            currentSongResId = playQueue[queueIndex];
            Song s = findSongByResId(currentSongResId);
            if (s != null) {
                currentTitle = s.getTitle();
            }
        } else {
            currentSongResId = songResIdFromIntent;
            currentTitle = intent.getStringExtra(EXTRA_TITLE);
        }

        startNewTrack(currentSongResId);
    }

    private void handlePlayPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
        } else {
            mediaPlayer.start();
            isPlaying = true;
        }
        showNotification();
        broadcastPlaybackState();
    }

    private void handleNext() {
        if (useQueue && playQueue != null && playQueue.length > 0) {
            queueIndex = (queueIndex + 1) % playQueue.length;
            currentSongResId = playQueue[queueIndex];
            Song next = findSongByResId(currentSongResId);
            if (next != null) {
                currentTitle = next.getTitle();
                startNewTrack(currentSongResId);
            }
        } else {
            Song next = getNextSongInRepository(currentSongResId);
            if (next != null) {
                currentSongResId = next.getResId();
                currentTitle = next.getTitle();
                startNewTrack(currentSongResId);
            }
        }
    }

    private void handlePrev() {
        if (useQueue && playQueue != null && playQueue.length > 0) {
            queueIndex = (queueIndex - 1);
            if (queueIndex < 0) queueIndex = playQueue.length - 1;
            currentSongResId = playQueue[queueIndex];
            Song prev = findSongByResId(currentSongResId);
            if (prev != null) {
                currentTitle = prev.getTitle();
                startNewTrack(currentSongResId);
            }
        } else {
            Song prev = getPrevSongInRepository(currentSongResId);
            if (prev != null) {
                currentSongResId = prev.getResId();
                currentTitle = prev.getTitle();
                startNewTrack(currentSongResId);
            }
        }
    }

    private void startNewTrack(int resId) {
        releasePlayer();
        mediaPlayer = MediaPlayer.create(this, resId);
        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            boolean autoNext = prefs.getBoolean(KEY_AUTO_NEXT, true);
            if (autoNext) {
                handleNext();
            } else {
                showNotification();
                broadcastPlaybackState();
            }
        });
        mediaPlayer.start();
        isPlaying = true;
        showNotification();
        broadcastPlaybackState();
    }
    private Song findSongByResId(int resId) {
        List<Song> activeSongs = SongRepository.getActiveSongs(this);
        for (Song song : activeSongs) {
            if (song.getResId() == resId) {
                return song;
            }
        }
        return null;
    }

    private Song getNextSongInRepository(int currentResId) {
        List<Song> activeSongs = SongRepository.getActiveSongs(this);
        if (activeSongs.isEmpty()) return null;
        for (int i = 0; i < activeSongs.size() - 1; i++) {
            if (activeSongs.get(i).getResId() == currentResId) {
                return activeSongs.get(i + 1);
            }
        }
        return activeSongs.get(0);
    }

    private Song getPrevSongInRepository(int currentResId) {
        List<Song> activeSongs = SongRepository.getActiveSongs(this);
        if (activeSongs.isEmpty()) return null;
        for (int i = 1; i < activeSongs.size(); i++) {
            if (activeSongs.get(i).getResId() == currentResId) {
                return activeSongs.get(i - 1);
            }
        }
        return activeSongs.get(activeSongs.size() - 1);
    }

    // ... (Phần còn lại của showNotification và các hàm khác)

    private void showNotification() {
        Intent openPlayerIntent = new Intent(this, PlayerActivity.class);
        openPlayerIntent.putExtra("song_id", currentSongResId);
        openPlayerIntent.putExtra("song_title", currentTitle);

        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openPlayerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );

        PendingIntent playPauseIntent = createActionIntent(ACTION_PLAY_PAUSE, 1);
        PendingIntent nextIntent = createActionIntent(ACTION_NEXT, 2);
        PendingIntent prevIntent = createActionIntent(ACTION_PREV, 3);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(currentTitle)
                .setContentText("MuseFlow") // Artist is not available in this new architecture
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentIntent(contentPendingIntent)
                .setShowWhen(false)
                .setOngoing(isPlaying)
                .setOnlyAlertOnce(true)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2))
                .addAction(R.drawable.ic_prev, "Prev", prevIntent)
                .addAction(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play",
                        playPauseIntent)
                .addAction(R.drawable.ic_next, "Next", nextIntent);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent createActionIntent(String action, int requestCode) {
        Intent i = new Intent(this, MusicService.class);
        i.setAction(action);
        return PendingIntent.getService(
                this,
                requestCode,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0)
        );
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID,
                    "MuseFlow Player",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(ch);
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void broadcastPlaybackState() {
        Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_SONG_ID, currentSongResId);
        intent.putExtra(EXTRA_IS_PLAYING, isPlaying);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}