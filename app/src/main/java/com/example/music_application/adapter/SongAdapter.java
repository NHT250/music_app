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

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private Context context;
    private List<Song> songList;
    private OnSongClickListener clickListener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongAdapter(Context context, List<Song> songList, OnSongClickListener clickListener) {
        this.context = context;
        this.songList = songList;
        this.clickListener = clickListener;
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
        holder.bind(song, clickListener);
    }

    @Override
    public int getItemCount() {
        return (songList != null) ? songList.size() : 0;
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView coverImageView;
        TextView titleTextView;
        TextView artistTextView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            coverImageView = itemView.findViewById(R.id.song_image);
            titleTextView = itemView.findViewById(R.id.song_title);
            artistTextView = itemView.findViewById(R.id.song_artist);
        }

        public void bind(final Song song, final OnSongClickListener clickListener) {
            titleTextView.setText(song.getTitle());
            artistTextView.setText(song.getArtist());

            Glide.with(itemView.getContext())
                    .load(song.getCover())
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(coverImageView);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onSongClick(position);
                    }
                }
            });
        }
    }
}
