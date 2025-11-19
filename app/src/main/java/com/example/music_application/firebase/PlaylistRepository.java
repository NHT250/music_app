package com.example.music_application.firebase;

import androidx.annotation.NonNull;

import com.example.music_application.model.Playlist;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {

    private final DatabaseReference databaseReference;

    public interface PlaylistListener {
        void onPlaylistLoaded(List<Playlist> playlists);
        void onError(Exception e);
    }

    public PlaylistRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("playlists");
    }

    public void createPlaylist(Playlist playlist, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        String id = databaseReference.child(playlist.getOwnerId()).push().getKey();
        playlist.setId(id);

        databaseReference.child(playlist.getOwnerId()).child(id).setValue(playlist)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    public void getUserPlaylists(String userId, PlaylistListener listener) {
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Playlist> playlists = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Playlist playlist = dataSnapshot.getValue(Playlist.class);
                    if (playlist != null) {
                        playlists.add(playlist);
                    }
                }
                listener.onPlaylistLoaded(playlists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public void addSongsToPlaylist(String userId, String playlistId, List<String> songIds, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(userId).child(playlistId).child("songIds").setValue(songIds)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    public void deletePlaylist(String userId, String playlistId, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(userId).child(playlistId).removeValue()
            .addOnSuccessListener(aVoid -> onSuccess.run())
            .addOnFailureListener(onFailure::accept);
    }
}
