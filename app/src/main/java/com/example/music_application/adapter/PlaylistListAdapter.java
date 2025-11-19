package com.example.music_application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Playlist;

import java.util.List;

public class PlaylistListAdapter extends RecyclerView.Adapter<PlaylistListAdapter.PlaylistViewHolder> {

    private List<Playlist> playlistList;
    private final OnPlaylistClickListener onPlaylistClickListener;
    private final OnDeletePlaylistClickListener onDeletePlaylistClickListener;

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    public interface OnDeletePlaylistClickListener {
        void onDeletePlaylistClick(Playlist playlist);
    }

    public PlaylistListAdapter(List<Playlist> playlistList, OnPlaylistClickListener onPlaylistClickListener, OnDeletePlaylistClickListener onDeletePlaylistClickListener) {
        this.playlistList = playlistList;
        this.onPlaylistClickListener = onPlaylistClickListener;
        this.onDeletePlaylistClickListener = onDeletePlaylistClickListener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlistList.get(position);
        holder.bind(playlist, onPlaylistClickListener, onDeletePlaylistClickListener);
    }

    @Override
    public int getItemCount() {
        return playlistList.size();
    }

    public void setPlaylists(List<Playlist> playlists) {
        this.playlistList = playlists;
        notifyDataSetChanged();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView playlistTitle;
        ImageView deleteButton;

        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistTitle = itemView.findViewById(R.id.playlist_title);
            deleteButton = itemView.findViewById(R.id.btn_delete_playlist);
        }

        public void bind(final Playlist playlist, final OnPlaylistClickListener listener, final OnDeletePlaylistClickListener deleteListener) {
            playlistTitle.setText(playlist.getTitle());
            itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
            deleteButton.setOnClickListener(v -> deleteListener.onDeletePlaylistClick(playlist));
        }
    }
}
