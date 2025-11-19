package com.example.music_application.player;

import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaSessionService {
    private static final String TAG = "MusicService";

    private MediaSession mediaSession = null;
    private ExoPlayer player = null;
    private List<Song> songList;
    private MusicServiceListener listener;

    private final IBinder binder = new MusicBinder();

    public interface MusicServiceListener {
        void onStateChanged();
        void onSongChanged(int position);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new ExoPlayer.Builder(this).build();
        mediaSession = new MediaSession.Builder(this, player).build();

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (listener != null) {
                    listener.onStateChanged();
                }
            }

            @Override
            public void onMediaItemTransition(@Nullable MediaItem mediaItem, int reason) {
                if (listener != null && mediaItem != null) {
                    int newIndex = player.getCurrentMediaItemIndex();
                    listener.onSongChanged(newIndex);
                }
            }
        });
    }

    @Nullable
    @Override
    public MediaSession onGetSession(@NonNull MediaSession.ControllerInfo controllerInfo) {
        return mediaSession;
    }

    public void setListener(MusicServiceListener listener) {
        this.listener = listener;
    }

    public void setSongList(List<Song> songs) {
        this.songList = songs;
        List<MediaItem> mediaItems = new ArrayList<>();
        for (Song song : songs) {
            MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                    .setTitle(song.getTitle())
                    .setArtist(song.getArtist())
                    .setArtworkUri(song.getCover() != null ? Uri.parse(song.getCover()) : null)
                    .build();

            MediaItem mediaItem = new MediaItem.Builder()
                    .setUri(song.getUrl())
                    .setMediaMetadata(mediaMetadata)
                    .build();
            mediaItems.add(mediaItem);
        }
        player.setMediaItems(mediaItems);
        player.prepare();
    }

    public void play(int position) {
        if (position >= 0 && position < player.getMediaItemCount()) {
            player.seekTo(position, 0);
            player.setPlayWhenReady(true);
            if(player.getPlaybackState() == Player.STATE_IDLE) {
                player.prepare();
            }
            player.play();
        }
    }

    public void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.setPlayWhenReady(true);
            if(player.getPlaybackState() == Player.STATE_IDLE) {
                player.prepare();
            }
            player.play();
        }
    }

    public void next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem();
        }
    }

    public void prev() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem();
        }
    }

    public void stop() {
        player.stop();
    }

    @Nullable
    public Song getCurrentSong() {
        int currentIndex = player.getCurrentMediaItemIndex();
        if (songList != null && currentIndex >= 0 && currentIndex < songList.size()) {
            return songList.get(currentIndex);
        }
        return null;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getCurrentPosition() {
        return (int) player.getCurrentPosition();
    }

    public int getDuration() {
        return (int) player.getDuration();
    }
    
    public void seekTo(int position) {
        player.seekTo(position);
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.release();
            player = null;
        }
        if (mediaSession != null) {
            mediaSession.release();
            mediaSession = null;
        }
        super.onDestroy();
    }
    
    // onBind is now handled by the base class, but we provide our binder for custom commands.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // For pre-Tirimasu MediaBrowserService compat, we need to handle onBind.
        // For MediaSessionService, the default onBind handles session connection.
        // We return our custom binder only for our own custom actions.
        if (SERVICE_INTERFACE.equals(intent.getAction())) {
             return super.onBind(intent);
        }
        return binder;
    }
}
