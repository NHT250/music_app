package com.example.music_application.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.List;

public class RecommendedAdapter extends BaseSongAdapter {

    private List<Song> songList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public RecommendedAdapter(Context context, List<Song> songList, OnSongClickListener listener) {
        super(context);
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_horizontal, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Song song = songList.get(position);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongClick(song);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    @Override
    protected Song getSongAt(int position) {
        return songList.get(position);
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }
}
