package com.example.music_application.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.List;

public class AdminSongAdapter extends RecyclerView.Adapter<AdminSongAdapter.SongViewHolder> {

    public interface OnSongActionListener {
        void onEdit(Song song);
        void onDelete(Song song);
    }

    private List<Song> songs;
    private OnSongActionListener listener;

    public AdminSongAdapter(List<Song> songs, OnSongActionListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    public void setSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_admin_crud, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.txtTitle.setText(song.getTitle());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(song);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(song);
        });
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        ImageButton btnEdit, btnDelete;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}