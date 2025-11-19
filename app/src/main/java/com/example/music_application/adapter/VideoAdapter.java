package com.example.music_application.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final Context context;
    private final List<Song> songList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onPlayClick(Song song);
        void onDownloadClick(Song song);
    }

    public VideoAdapter(Context context, List<Song> songList, OnItemClickListener listener) {
        this.context = context;
        this.songList = songList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.bind(song, listener);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;
        TextView statusTextView;
        ImageButton playButton;
        ImageButton downloadButton;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            playButton = itemView.findViewById(R.id.playButton);
            downloadButton = itemView.findViewById(R.id.downloadButton);
        }

        public void bind(final Song song, final OnItemClickListener listener) {
            titleTextView.setText(song.getTitle());

            Glide.with(itemView.getContext())
                    .load(song.getImageUrl())
                    .placeholder(R.mipmap.ic_launcher)
                    .into(thumbnailImageView);

            if (song.isDownloaded()) {
                statusTextView.setText("Offline");
                downloadButton.setVisibility(View.GONE);
            } else {
                statusTextView.setText("Online");
                downloadButton.setVisibility(View.VISIBLE);
            }

            playButton.setOnClickListener(v -> listener.onPlayClick(song));
            downloadButton.setOnClickListener(v -> listener.onDownloadClick(song));
        }
    }
}
