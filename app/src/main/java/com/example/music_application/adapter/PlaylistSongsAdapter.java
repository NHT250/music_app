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

public class PlaylistSongsAdapter extends RecyclerView.Adapter<PlaylistSongsAdapter.SongViewHolder> {

    private List<Song> songList;
    private final OnSongClickListener onSongClickListener;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Song song);
    }

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public PlaylistSongsAdapter(List<Song> songList, OnSongClickListener onSongClickListener, OnDeleteClickListener onDeleteClickListener) {
        this.songList = songList;
        this.onSongClickListener = onSongClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_playlist, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, onSongClickListener, onDeleteClickListener);
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
        ImageView songImageView;
        TextView titleTextView, artistTextView;
        ImageView deleteButton;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            songImageView = itemView.findViewById(R.id.song_image);
            titleTextView = itemView.findViewById(R.id.song_title);
            artistTextView = itemView.findViewById(R.id.song_artist);
            deleteButton = itemView.findViewById(R.id.btn_delete_song);
        }

        public void bind(final Song song, final OnSongClickListener songClickListener, final OnDeleteClickListener deleteClickListener) {
            titleTextView.setText(song.getTitle());
            artistTextView.setVisibility(View.GONE);

            Glide.with(itemView.getContext())
                    .load(song.getImageUrl())
                    .placeholder(R.drawable.ic_default_music_logo)
                    .error(R.drawable.ic_default_music_logo)
                    .into(songImageView);

            itemView.setOnClickListener(v -> songClickListener.onSongClick(song));
            deleteButton.setOnClickListener(v -> deleteClickListener.onDeleteClick(song));
        }
    }
}
