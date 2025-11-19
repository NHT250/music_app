package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.adapter.RecommendedSongAdapter;
import com.example.music_application.adapter.TopHitsAdapter;
import com.example.music_application.model.Song;
import com.example.music_application.player.PlayerManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements View.OnClickListener, RecommendedSongAdapter.OnSongClickListener {

    private RecyclerView topHitsRecyclerView, recommendedRecyclerView;
    private TopHitsAdapter topHitsAdapter;
    private RecommendedSongAdapter recommendedAdapter;

    private List<Song> allSongsList; // Stores all songs
    private List<Song> topHitsList; // For Top Hits RecyclerView
    private List<Song> recommendedList; // For Recommended RecyclerView

    private DatabaseReference databaseReference;
    private PlayerManager playerManager;

    private ImageView btnProfile, btnPlaylistList, btnLogout, btnSettings;
    private Button btnPop, btnIndie, btnHiphop, btnBallad;
    private TextView recommendedTitle;
    private RelativeLayout bottomPlayingBar;
    private TextView playingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        playerManager = PlayerManager.getInstance();

        initViews();
        setupClickListeners();
        setupRecyclerViews();

        databaseReference = FirebaseDatabase.getInstance().getReference("songs");
        fetchSongs();
    }

    private void initViews() {
        topHitsRecyclerView = findViewById(R.id.rv_top_hits);
        recommendedRecyclerView = findViewById(R.id.rv_recommended);
        btnProfile = findViewById(R.id.btn_profile);
        btnPlaylistList = findViewById(R.id.btn_playlist_list);
        btnLogout = findViewById(R.id.btn_logout);
        btnSettings = findViewById(R.id.btn_settings);
        btnPop = findViewById(R.id.btn_pop);
        btnIndie = findViewById(R.id.btn_indie);
        btnHiphop = findViewById(R.id.btn_hiphop);
        btnBallad = findViewById(R.id.btn_ballad);
        recommendedTitle = findViewById(R.id.recommended_title);
        bottomPlayingBar = findViewById(R.id.bottom_playing_bar);
        playingText = findViewById(R.id.playing_text);
    }

    private void setupClickListeners() {
        btnProfile.setOnClickListener(this);
        btnPlaylistList.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnPop.setOnClickListener(this);
        btnIndie.setOnClickListener(this);
        btnHiphop.setOnClickListener(this);
        btnBallad.setOnClickListener(this);
        recommendedTitle.setOnClickListener(this);
    }

    private void setupRecyclerViews() {
        // Top Hits (Horizontal)
        topHitsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topHitsList = new ArrayList<>();
        topHitsAdapter = new TopHitsAdapter(this, topHitsList, position -> onSongClick(topHitsList.get(position)));
        topHitsRecyclerView.setAdapter(topHitsAdapter);

        // Recommended (Vertical)
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendedList = new ArrayList<>();
        recommendedAdapter = new RecommendedSongAdapter(this, recommendedList, this);
        recommendedRecyclerView.setAdapter(recommendedAdapter);
    }

    private void fetchSongs() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allSongsList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Song song = dataSnapshot.getValue(Song.class);
                    if (song != null) {
                        allSongsList.add(song);
                    }
                }
                // Initially, display all songs in both lists
                filterSongs(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserHomeActivity.this, "Failed to load songs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterSongs(String category) {
        List<Song> filteredList = new ArrayList<>();
        if (category == null) {
            filteredList.addAll(allSongsList);
        } else {
            for (Song song : allSongsList) {
                if (category.equalsIgnoreCase(song.getCategory())) {
                    filteredList.add(song);
                }
            }
        }

        if (filteredList.isEmpty() && category != null) {
            Toast.makeText(this, "No songs found in this category", Toast.LENGTH_SHORT).show();
        }

        // Update Top Hits list
        topHitsList.clear();
        if (filteredList.size() > 5) {
            topHitsList.addAll(filteredList.subList(0, 5));
        } else {
            topHitsList.addAll(filteredList);
        }
        topHitsAdapter.setSongs(topHitsList);

        // Update Recommended list with shuffled songs
        recommendedList.clear();
        List<Song> shuffledList = new ArrayList<>(allSongsList);
        Collections.shuffle(shuffledList);
        recommendedList.addAll(shuffledList);
        recommendedAdapter.setSongs(recommendedList);
    }

    public void onSongClick(Song song) {
        int position = -1;
        for (int i = 0; i < recommendedList.size(); i++) {
            if (recommendedList.get(i).getId().equals(song.getId())) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            playerManager.setSongList(recommendedList);

            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("song_position", position);
            startActivity(intent);

            Song clickedSong = recommendedList.get(position);
            playingText.setText("Playing: " + clickedSong.getTitle());
            bottomPlayingBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSongClick(int position) {
        onSongClick(recommendedList.get(position));
    }

    @Override
    public void onLikeClick(int position) {
        Song song = recommendedList.get(position);
        Toast.makeText(this, "Liked " + song.getTitle(), Toast.LENGTH_SHORT).show();
        // Implement like functionality here
    }

    @Override
    public void onAddToPlaylistClick(int position) {
        Song song = recommendedList.get(position);
        Toast.makeText(this, "Added " + song.getTitle() + " to playlist", Toast.LENGTH_SHORT).show();
        // Implement add to playlist functionality here
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.btn_playlist_list) {
            startActivity(new Intent(this, PlaylistListActivity.class));
        } else if (id == R.id.btn_logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (id == R.id.btn_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.btn_pop) {
            filterSongs("POP");
        } else if (id == R.id.btn_indie) {
            filterSongs("INDIE");
        } else if (id == R.id.btn_hiphop) {
            filterSongs("HIP-HOP");
        } else if (id == R.id.btn_ballad) {
            filterSongs("BALLAD");
        } else if (id == R.id.recommended_title) {
            filterSongs(null); // Show all songs
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
