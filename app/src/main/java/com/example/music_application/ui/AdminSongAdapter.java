package com.example.music_application.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.Song;

import java.util.ArrayList;
import java.util.List;

public class AdminSongAdapter extends RecyclerView.Adapter<AdminSongAdapter.AdminSongViewHolder> {

    public interface OnAdminSongListener {
        void onEdit(Song song);
        void onDelete(Song song);
    }

    private List<Song> originalList = new ArrayList<>();
    private List<Song> filteredList = new ArrayList<>();
    private OnAdminSongListener listener;

    public AdminSongAdapter(OnAdminSongListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        originalList.clear();
        originalList.addAll(songs);
        filteredList.clear();
        filteredList.addAll(songs);
        notifyDataSetChanged();
    }

    public void filter(String keyword) {
        filteredList.clear();
        if (keyword == null || keyword.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lower = keyword.toLowerCase();
            for (Song s : originalList) {
                if (s.getTitle().toLowerCase().contains(lower)
                        || s.getArtist().toLowerCase().contains(lower)
                        || s.getFilePath().toLowerCase().contains(lower)) {
                    filteredList.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_song, parent, false);
        return new AdminSongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminSongViewHolder holder, int position) {
        Song song = filteredList.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());
        holder.txtPath.setText(song.getFilePath());

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(song);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(song);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class AdminSongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist, txtPath;
        ImageView btnEdit, btnDelete;

        AdminSongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            txtPath = itemView.findViewById(R.id.txtPath);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}