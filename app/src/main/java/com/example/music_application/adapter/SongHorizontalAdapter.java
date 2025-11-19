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

public class SongHorizontalAdapter extends RecyclerView.Adapter<SongHorizontalAdapter.SongHorizontalViewHolder> {

    private Context context;
    private List<Song> songList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(int position);
    }

    public SongHorizontalAdapter(Context context, List<Song> songList, OnSongClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongHorizontalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_horizontal, parent, false);
        return new SongHorizontalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongHorizontalViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());
        Glide.with(context).load(song.getCover()).into(holder.imgCover);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    class SongHorizontalViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtArtist;

        public SongHorizontalViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_song_cover_horizontal);
            txtTitle = itemView.findViewById(R.id.txt_song_title_horizontal);
            txtArtist = itemView.findViewById(R.id.txt_song_artist_horizontal);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onSongClick(position);
                    }
                }
            });
        }
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }
}
