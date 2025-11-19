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

public class UserSongAdapter extends RecyclerView.Adapter<UserSongAdapter.SongViewHolder> {

    private static final int VIEW_TYPE_CARD = 0;
    private static final int VIEW_TYPE_LIST = 1;

    private final Context context;
    private List<Song> songList;
    private final OnSongClickListener listener;
    private final boolean isCardView;

    public interface OnSongClickListener {
        void onSongClick(Song song, int position, List<Song> currentList);
    }

    public UserSongAdapter(Context context, List<Song> songs, OnSongClickListener listener, boolean isCardView) {
        this.context = context;
        this.songList = songs;
        this.listener = listener;
        this.isCardView = isCardView;
    }

    public void updateSongs(List<Song> newSongs) {
        this.songList = newSongs;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isCardView ? VIEW_TYPE_CARD : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_CARD) {
            view = LayoutInflater.from(context).inflate(R.layout.item_song_card, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.item_song_user, parent, false);
        }
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

        public void bind(final Song song, final int position, final OnSongClickListener listener, final List<Song> currentList) {
            txtSongTitle.setText(song.getTitle());
            // txtSongArtist.setText(song.getArtist()); // This method was removed
            if (txtSongArtist != null) {
                txtSongArtist.setVisibility(View.GONE);
            }

            Glide.with(itemView.getContext())
                    .load(song.getImageUrl())
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(imgSongCover);

            itemView.setOnClickListener(v -> listener.onSongClick(song, position, currentList));
        }
    }
}
