package com.example.music_application.player;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.music_application.model.Song;
import java.io.IOException;
import java.util.List;

/**
 * Lớp Singleton để quản lý MediaPlayer.
 */
public class PlayerManager {
    private static final String TAG = "PlayerManager";
    private static PlayerManager instance;
    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private List<Song> songList;

    private PlayerManager() {
    }

    public static synchronized PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    public List<Song> getSongList() {
        return songList;
    }

    public void play(int position) {
        if (songList == null || position < 0 || position >= songList.size()) {
            return;
        }

        currentSongIndex = position;
        Song songToPlay = songList.get(currentSongIndex);

        String url = songToPlay.getUrl();
        if (url == null || url.isEmpty()) {
            Log.e(TAG, "URL của bài hát rỗng hoặc null!");
            return;
        }

        release();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer Error! What: " + what + ", Extra: " + extra);
                release();
                return true;
            });
            mediaPlayer.setOnCompletionListener(mp -> next());
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);

        } catch (IOException e) {
            Log.e(TAG, "Lỗi setDataSource: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void next() {
        if (songList != null && !songList.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList.size();
            play(currentSongIndex);
        }
    }

    public void previous() {
        if (songList != null && !songList.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songList.size()) % songList.size();
            play(currentSongIndex);
        }
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getDuration();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            try {
                return mediaPlayer.getCurrentPosition();
            } catch (IllegalStateException e) {
                return 0;
            }
        }
        return 0;
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public Song getCurrentSong() {
        if (songList != null && currentSongIndex != -1) {
            return songList.get(currentSongIndex);
        }
        return null;
    }

    public void setSongList(List<Song> songList) {
        this.songList = songList;
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}
