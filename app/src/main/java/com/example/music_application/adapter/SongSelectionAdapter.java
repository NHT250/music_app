package com.example.music_application.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SongSelectionAdapter extends RecyclerView.Adapter<SongSelectionAdapter.ViewHolder> {

    private List<Song> allSongs;
    private Set<String> selectedSongIds = new HashSet<>();

    public SongSelectionAdapter(List<Song> allSongs, List<String> currentSongIds) {
        this.allSongs = allSongs;
        if (currentSongIds != null) {
            this.selectedSongIds.addAll(currentSongIds);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_selection, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = allSongs.get(position);
        holder.title.setText(song.getTitle());
        // holder.artist.setText(song.getArtist()); // This method was removed
        if (holder.artist != null) {
            holder.artist.setVisibility(View.GONE);
        }

        holder.checkBox.setChecked(selectedSongIds.contains(song.getId()));

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedSongIds.add(song.getId());
            } else {
                selectedSongIds.remove(song.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return allSongs.size();
    }

    public List<String> getSelectedSongIds() {
        return new ArrayList<>(selectedSongIds);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView title, artist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkboxSong);
            title = itemView.findViewById(R.id.song_title);
            artist = itemView.findViewById(R.id.song_artist);
        }
    }
}
