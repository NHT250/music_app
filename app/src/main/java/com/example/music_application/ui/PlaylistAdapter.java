package com.example.music_application.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.Playlist;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    public interface PlaylistListener {
        void onPlaylistClick(Playlist playlist);
        void onDeleteClick(Playlist playlist);
    }

    private List<Playlist> playlists = new ArrayList<>();
    private final PlaylistListener listener;

    public PlaylistAdapter(PlaylistListener listener) {
        this.listener = listener;
    }

    public void setPlaylists(List<Playlist> list) {
        this.playlists.clear();
        this.playlists.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new PlaylistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist p = playlists.get(position);
        holder.txtPlaylistName.setText(p.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onPlaylistClick(p);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView txtPlaylistName;
        ImageButton btnDelete;

        PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlaylistName = itemView.findViewById(R.id.txtPlaylistName);
            btnDelete = itemView.findViewById(R.id.btnDeletePlaylist);
        }
    }
}