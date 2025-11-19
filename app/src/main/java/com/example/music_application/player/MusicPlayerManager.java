package com.example.music_application.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import com.example.music_application.model.Song;

import java.io.IOException;

public class MusicPlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private static MusicPlayerManager instance;
    private MediaPlayer mediaPlayer;
    private PlayerListener playerListener;
    private final Handler handler = new Handler();
    private Song currentSong;

    public interface PlayerListener {
        void onStateChanged(PlayerState state);
        void onProgressUpdate(int currentPosition, int duration);
        void onSongCompleted();
    }

    public enum PlayerState {
        PLAYING, PAUSED, STOPPED, PREPARING
    }

    private MusicPlayerManager() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
    }

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) {
            instance = new MusicPlayerManager();
        }
        return instance;
    }

    public void setPlayerListener(PlayerListener listener) {
        this.playerListener = listener;
    }

    public void play(Song song) {
        this.currentSong = song;
        try {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getAudioUrl());
            mediaPlayer.prepareAsync();
            if (playerListener != null) {
                playerListener.onStateChanged(PlayerState.PREPARING);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (playerListener != null) {
                playerListener.onStateChanged(PlayerState.PAUSED);
            }
            handler.removeCallbacks(progressUpdater);
        }
    }

    public void resume() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            if (playerListener != null) {
                playerListener.onStateChanged(PlayerState.PLAYING);
            }
            updateProgress();
        }
    }

    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        instance = null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if (playerListener != null) {
            playerListener.onStateChanged(PlayerState.PLAYING);
        }
        updateProgress();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playerListener != null) {
            playerListener.onSongCompleted();
        }
        handler.removeCallbacks(progressUpdater);
    }

    private void updateProgress() {
        if (mediaPlayer != null && playerListener != null) {
            handler.post(progressUpdater);
        }
    }

    private final Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                playerListener.onProgressUpdate(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration());
                handler.postDelayed(this, 1000);
            }
        }
    };
}
