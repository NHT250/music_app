package com.example.music_application.ui;

import com.example.music_application.model.Song;

/**
 * A common interface for handling song actions across different adapters.
 */
public interface OnSongActionClickListener {
    void onSongClick(Song song);
    void onFavoriteClick(Song song);
    void onAddToPlaylistClick(Song song);
}
