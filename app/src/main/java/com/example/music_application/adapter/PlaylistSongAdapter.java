package com.example.music_application.adapter;

import android.content.Context;
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

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.SongViewHolder> {

    private final Context context;
    private List<Song> songList;
    private final OnPlaylistSongClickListener listener;

    public interface OnPlaylistSongClickListener {
        void onSongClick(Song song, int position, List<Song> currentList);
        void onSongLongClick(Song song);
    }

    public PlaylistSongAdapter(Context context, List<Song> songList, OnPlaylistSongClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    public void updateSongs(List<Song> newSongs) {
        this.songList = newSongs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_user, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, position, listener, songList);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSongCover;
        TextView txtSongTitle, txtSongArtist;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSongCover = itemView.findViewById(R.id.imgSongCover);
            txtSongTitle = itemView.findViewById(R.id.txtSongTitle);
            txtSongArtist = itemView.findViewById(R.id.txtSongArtist);
        }

        public void bind(final Song song, final int position, final OnPlaylistSongClickListener listener, final List<Song> currentList) {
            txtSongTitle.setText(song.getTitle());
            if (txtSongArtist != null) {
                txtSongArtist.setText(song.getArtist());
                txtSongArtist.setVisibility(View.VISIBLE);
            }

            Glide.with(itemView.getContext())
                    .load(song.getImageUrl())
                    .placeholder(R.drawable.ic_music_note)
                    .into(imgSongCover);

            itemView.setOnClickListener(v -> listener.onSongClick(song, getAdapterPosition(), currentList));

            itemView.setOnLongClickListener(v -> {
                listener.onSongLongClick(song);
                return true;
            });
        }
    }
}
