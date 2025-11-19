package com.example.music_application.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.music_application.model.Song;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    private final DatabaseReference databaseReference;

    public interface SongListListener {
        void onSongListLoaded(List<Song> songs);
        void onError(Exception e);
    }

    public interface SongLikeListener {
        void onLikeSuccess();
        void onLikeFailure(Exception e);
    }

    public SongRepository() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("songs");
    }

    public void addSong(Song song, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        String id = databaseReference.push().getKey();
        song.setId(id);
        databaseReference.child(id).setValue(song)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    public void updateSong(Song song, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(song.getId()).setValue(song)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    public void deleteSong(String songId, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(songId).removeValue()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onFailure::accept);
    }

    public void getAllSongs(SongListListener listener) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Song> songs = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Song song = dataSnapshot.getValue(Song.class);
                    if (song != null) {
                        songs.add(song);
                    }
                }
                listener.onSongListLoaded(songs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.toException());
            }
        });
    }

    public Query getSongsByCategory(String category) {
        return databaseReference.orderByChild("category").equalTo(category);
    }

    public Query searchSongs(String query) {
        return databaseReference.orderByChild("title")
                .startAt(query)
                .endAt(query + "\uf8ff");
    }

    public void likeSong(String songId, int newLikeCount, SongLikeListener listener) {
        databaseReference.child(songId).child("likes").setValue(newLikeCount)
            .addOnSuccessListener(aVoid -> listener.onLikeSuccess())
            .addOnFailureListener(listener::onLikeFailure);
    }

    public void incrementLikes(String songId, Runnable onSuccess, java.util.function.Consumer<Exception> onFailure) {
        databaseReference.child(songId).child("likes").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long currentValue = mutableData.getValue(Long.class);
                if (currentValue == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue(currentValue + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    onSuccess.run();
                } else {
                    onFailure.accept(error.toException());
                }
            }
        });
    }
}
