package com.example.music_application.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private List<Song> songs = new ArrayList<>();
    private List<Song> originalSongs = new ArrayList<>(); // For filtering
    private final OnSongActionClickListener listener;

    public SongAdapter(OnSongActionClickListener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> songs) {
        this.songs.clear();
        this.songs.addAll(songs);
        this.originalSongs.clear();
        this.originalSongs.addAll(songs);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        songs.clear();
        if (query.isEmpty()) {
            songs.addAll(originalSongs);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Song song : originalSongs) {
                if (song.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    songs.add(song);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the correct layout file: item_song.xml
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText("MuseFlow"); // Set a default artist for now

        holder.itemView.setOnClickListener(v -> listener.onSongClick(song));

        // Set listeners for the new buttons
        if (holder.imgFavorite != null) {
            holder.imgFavorite.setOnClickListener(v -> listener.onFavoriteClick(song));
        }
        if (holder.imgAddToPlaylist != null) {
            holder.imgAddToPlaylist.setOnClickListener(v -> listener.onAddToPlaylistClick(song));
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist;
        ImageView imgThumb, imgFavorite, imgAddToPlaylist;

        SongViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            imgAddToPlaylist = itemView.findViewById(R.id.imgAddToPlaylist);
        }
    }
}
