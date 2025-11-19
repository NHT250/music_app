package com.example.music_application.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.NotificationTarget;
import com.example.music_application.R;
import com.example.music_application.model.Song;
import com.example.music_application.ui.PlayerActivity;

import java.io.IOException;

import static com.example.music_application.model.MusicConstant.ACTION_PAUSE;
import static com.example.music_application.model.MusicConstant.ACTION_RESUME;
import static com.example.music_application.model.MusicConstant.ACTION_STOP;
import static com.example.music_application.model.MusicConstant.CHANNEL_ID;
import static com.example.music_application.model.MusicConstant.NOTIFICATION_ID;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    private final IBinder binder = new MusicBinder();
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private Song mSong;

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey("song")) {
            Song song = (Song) bundle.getSerializable("song");
            if (song != null) {
                mSong = song;
                startMusic(mSong);
            }
        }

        int action = intent.getIntExtra("MUSIC_ACTION", 0);
        if (action != 0) {
            handleMusicAction(action);
        }

        return START_NOT_STICKY;
    }

    private void startMusic(Song song) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        try {
            String url = song.getUrl();
            if (url != null && !url.isEmpty()) {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepareAsync(); // Prepare for streaming
            } else {
                Toast.makeText(this, "Song has no URL!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MusicService", "Error setting data source", e);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start(); // Start playback once prepared
        isPlaying = true;
        sendNotification(mSong);
        sendMusicAction(ACTION_RESUME);
    }

    private void handleMusicAction(int action) {
        switch (action) {
            case ACTION_PAUSE: pauseMusic(); break;
            case ACTION_RESUME: resumeMusic(); break;
            case ACTION_STOP: stopMusic(); break;
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            if (mSong != null) sendNotification(mSong);
            sendMusicAction(ACTION_PAUSE);
        }
    }

    public void resumeMusic() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            if (mSong != null) sendNotification(mSong);
            sendMusicAction(ACTION_RESUME);
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        stopForeground(true);
        stopSelf();
        isPlaying = false;
        sendMusicAction(ACTION_STOP);
    }

    public void seekMusic(int progress) { // Added this method back
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
        }
    }

    public Song getCurrentSong() {
        return mSong;
    }

    private void sendNotification(Song song) {
        createNotificationChannel();

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song", song);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.mini_player);
        remoteViews.setTextViewText(R.id.text_song_name, song.getTitle());
        remoteViews.setTextViewText(R.id.text_artist_name, song.getArtist());

        remoteViews.setImageViewResource(R.id.button_play_pause, isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
        remoteViews.setOnClickPendingIntent(R.id.button_play_pause, getPendingIntent(this, isPlaying ? ACTION_PAUSE : ACTION_RESUME));
        remoteViews.setOnClickPendingIntent(R.id.button_close, getPendingIntent(this, ACTION_STOP));

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setSound(null)
                .build();

        NotificationTarget notificationTarget = new NotificationTarget(this, R.id.image_song, remoteViews, notification, NOTIFICATION_ID);

        String imageUrl = song.getCover();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).asBitmap().load(imageUrl).into(notificationTarget);
        } else {
            remoteViews.setImageViewResource(R.id.image_song, R.drawable.ic_music);
        }

        startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, MusicReceiver.class);
        intent.putExtra("MUSIC_ACTION", action);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Channel", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void sendMusicAction(int action) {
        Intent intent = new Intent("MUSIC_ACTION");
        intent.putExtra("MUSIC_ACTION", action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public boolean isPlaying() {
        return isPlaying && mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public int getDuration() { return (mediaPlayer != null && isPlaying) ? mediaPlayer.getDuration() : 0; }
    public int getCurrentPosition() { return (mediaPlayer != null && isPlaying) ? mediaPlayer.getCurrentPosition() : 0; }

    @Nullable @Override public IBinder onBind(Intent intent) { return binder; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
