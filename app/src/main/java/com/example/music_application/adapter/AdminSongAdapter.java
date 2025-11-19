package com.example.music_application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.List;

public class AdminSongAdapter extends RecyclerView.Adapter<AdminSongAdapter.SongViewHolder> {

    private List<Song> songList;
    private final OnSongListener onSongListener;

    public interface OnSongListener {
        void onEditClick(Song song);
        void onDeleteClick(Song song);
    }

    public AdminSongAdapter(List<Song> songList, OnSongListener onSongListener) {
        this.songList = songList;
        this.onSongListener = onSongListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, onSongListener);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView songImageView, editIcon, deleteIcon;
        TextView titleTextView, artistTextView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songImageView = itemView.findViewById(R.id.song_image);
            titleTextView = itemView.findViewById(R.id.song_title);
            artistTextView = itemView.findViewById(R.id.song_artist);
            editIcon = itemView.findViewById(R.id.icon_edit);
            deleteIcon = itemView.findViewById(R.id.icon_delete);
        }

        public void bind(final Song song, final OnSongListener listener) {
            titleTextView.setText(song.getTitle());
            // artistTextView.setText(song.getArtist()); // This method was removed from the Song model
            artistTextView.setVisibility(View.GONE);

            Glide.with(itemView.getContext())
                    .load(song.getImageUrl())
                    .placeholder(R.drawable.ic_default_music_logo)
                    .error(R.drawable.ic_default_music_logo)
                    .into(songImageView);

            editIcon.setOnClickListener(v -> listener.onEditClick(song));
            deleteIcon.setOnClickListener(v -> listener.onDeleteClick(song));
        }
    }
}
