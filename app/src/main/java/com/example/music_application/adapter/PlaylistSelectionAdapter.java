package com.example.music_application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Playlist;

import java.util.List;

public class PlaylistSelectionAdapter extends RecyclerView.Adapter<PlaylistSelectionAdapter.PlaylistViewHolder> {

    private List<Playlist> playlists;
    private OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public PlaylistSelectionAdapter(List<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_selection, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist, listener);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        private TextView playlistName;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlist_name);
        }

        public void bind(final Playlist playlist, final OnPlaylistClickListener listener) {
            playlistName.setText(playlist.getTitle());
            itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
        }
    }
}
