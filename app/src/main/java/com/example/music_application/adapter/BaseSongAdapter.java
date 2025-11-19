package com.example.music_application.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_application.R;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;
import com.example.music_application.ui.AddSongToPlaylistActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public abstract class BaseSongAdapter extends RecyclerView.Adapter<BaseSongAdapter.SongViewHolder> {

    protected Context context;
    protected SongRepository songRepository;

    public BaseSongAdapter(Context context) {
        this.context = context;
        this.songRepository = new SongRepository();
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = getSongAt(position);
        holder.bind(song);
    }

    protected abstract Song getSongAt(int position);

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgSongCover, ivLike, ivAddToPlaylist;
        private TextView txtSongTitle, txtSongArtist, txtLikeCount;
        private DatabaseReference likeRef;
        private ValueEventListener likeListener;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSongCover = itemView.findViewById(R.id.img_song_cover_horizontal);
            txtSongTitle = itemView.findViewById(R.id.txt_song_title_horizontal);
            txtSongArtist = itemView.findViewById(R.id.txt_song_artist_horizontal);
            ivLike = itemView.findViewById(R.id.iv_like);
            ivAddToPlaylist = itemView.findViewById(R.id.iv_add_to_playlist);
            txtLikeCount = itemView.findViewById(R.id.txt_like_count);
        }

        public void bind(final Song song) {
            txtSongTitle.setText(song.getTitle());
            txtSongArtist.setText(song.getArtist());
            Glide.with(context).load(song.getCover()).into(imgSongCover);
            txtLikeCount.setText(String.valueOf(song.getLikes()));

            if (likeRef != null && likeListener != null) {
                likeRef.removeEventListener(likeListener);
            }

            likeRef = FirebaseDatabase.getInstance().getReference("songs").child(song.getId()).child("likes");
            likeListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Long likes = snapshot.getValue(Long.class);
                    if (likes != null) {
                        txtLikeCount.setText(String.valueOf(likes));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // No-op
                }
            };
            likeRef.addValueEventListener(likeListener);

            ivLike.setOnClickListener(v -> {
                songRepository.incrementLikes(song.getId(),
                        () -> { /* UI is updated by listener */ },
                        e -> Toast.makeText(context, "Failed to like.", Toast.LENGTH_SHORT).show());
            });

            ivAddToPlaylist.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddSongToPlaylistActivity.class);
                intent.putExtra("SONG_ID", song.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull SongViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.likeRef != null && holder.likeListener != null) {
            holder.likeRef.removeEventListener(holder.likeListener);
        }
    }
}
