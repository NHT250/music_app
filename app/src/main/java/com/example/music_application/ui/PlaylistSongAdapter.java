package com.example.music_application.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.VH> {

    public interface Listener {
        void onSongClick(Song song);
        void onSongLongClick(Song song);
    }

    private List<Song> songs = new ArrayList<>();
    private Listener listener;

    public PlaylistSongAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setSongs(List<Song> list) {
        songs.clear();
        songs.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Song song = songs.get(position);
        holder.txtTitle.setText(song.getTitle());
        holder.txtArtist.setText(song.getArtist());

        if (song.getCoverPath() != null && !song.getCoverPath().isEmpty()) {
            holder.imgThumb.setImageURI(android.net.Uri.parse(song.getCoverPath()));
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_music_note);
        }

        // trong playlist detail, icon add-to-playlist không cần dùng
        holder.imgAddToPlaylist.setVisibility(View.GONE);
        // nếu muốn, có thể ẩn favorite luôn:
        // holder.imgFavorite.setVisibility(View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onSongLongClick(song);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTitle, txtArtist;
        ImageView imgThumb, imgFavorite, imgAddToPlaylist;

        VH(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtArtist = itemView.findViewById(R.id.txtArtist);
            imgThumb = itemView.findViewById(R.id.imgThumb);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            imgAddToPlaylist = itemView.findViewById(R.id.imgAddToPlaylist);
        }
    }
}