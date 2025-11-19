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

public class TopHitsAdapter extends RecyclerView.Adapter<TopHitsAdapter.TopHitsViewHolder> {

    private Context context;
    private List<Song> songList;
    private OnSongClickListener listener;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public TopHitsAdapter(Context context, List<Song> songList, OnSongClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopHitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_top_hit, parent, false);
        return new TopHitsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopHitsViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());
        Glide.with(context).load(song.getCover()).into(holder.imgCover);

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

    class TopHitsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView txtTitle, txtArtist;

        public TopHitsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_song_cover);
            txtTitle = itemView.findViewById(R.id.txt_song_title);
            txtArtist = itemView.findViewById(R.id.txt_song_artist);
        }
    }

    public void setSongs(List<Song> songs) {
        this.songList = songs;
        notifyDataSetChanged();
    }
}
