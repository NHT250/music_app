package com.example.music_application.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity implements OnSongActionClickListener {

    // Views
    private RecyclerView rvGridSongs, rvListSongs;
    private TextView txtEmpty;
    private Button btnCatPop, btnCatIndie, btnCatHipHop, btnCatBallad;

    // Data
    private List<Song> allActiveSongs;
    private String currentCategory = "Pop"; // Mặc định lọc theo Pop

    // Adapters
    private SongGridAdapter gridAdapter;
    private SongAdapter listAdapter;
    
    // User Info
    private long userIdFromLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        userIdFromLogin = getIntent().getLongExtra("user_id", -1);

        // --- Ánh xạ Views ---
        setupViews();

        // --- Cài đặt RecyclerViews ---
        setupRecyclerViews();

        // --- Gán sự kiện Click ---
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSongsAndFilter();
    }

    private void setupViews() {
        rvGridSongs = findViewById(R.id.rvGridSongs);
        rvListSongs = findViewById(R.id.rvListSongs);
        txtEmpty = findViewById(R.id.txtEmpty);
        btnCatPop = findViewById(R.id.btnCatPop);
        btnCatIndie = findViewById(R.id.btnCatIndie);
        btnCatHipHop = findViewById(R.id.btnCatHipHop);
        btnCatBallad = findViewById(R.id.btnCatBallad);
    }

    private void setupRecyclerViews() {
        // Khởi tạo Adapter với listener là "this" (UserHomeActivity)
        gridAdapter = new SongGridAdapter(this);
        rvGridSongs.setLayoutManager(new GridLayoutManager(this, 2));
        rvGridSongs.setAdapter(gridAdapter);

        listAdapter = new SongAdapter(this);
        rvListSongs.setLayoutManager(new LinearLayoutManager(this));
        rvListSongs.setAdapter(listAdapter);
    }

    private void setupClickListeners() {
        // Category buttons
        btnCatPop.setOnClickListener(v -> setCategory("Pop"));
        btnCatIndie.setOnClickListener(v -> setCategory("Indie"));
        btnCatHipHop.setOnClickListener(v -> setCategory("Hip-Hop"));
        btnCatBallad.setOnClickListener(v -> setCategory("Ballad"));

        // Header buttons
        ImageView btnSettings = findViewById(R.id.btnSettings);
        ImageView btnLogout = findViewById(R.id.btnLogout);
        ImageView btnPlaylists = findViewById(R.id.btnPlaylists);
        ImageView btnProfile = findViewById(R.id.btnProfile);

        if(btnSettings != null) btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        if(btnLogout != null) btnLogout.setOnClickListener(v -> logout());
        if(btnPlaylists != null) btnPlaylists.setOnClickListener(v -> {
            Intent i = new Intent(this, PlaylistListActivity.class);
            i.putExtra("user_id", userIdFromLogin);
            startActivity(i);
        });
        if(btnProfile != null) btnProfile.setOnClickListener(v -> {
            Intent i = new Intent(this, UserProfileActivity.class);
            i.putExtra("user_id", userIdFromLogin);
            startActivity(i);
        });
    }

    private void loadSongsAndFilter() {
        // 1. Lấy toàn bộ bài hát được Admin bật
        allActiveSongs = SongRepository.getActiveSongs(this);
        // 2. Lọc và hiển thị theo category đang được chọn
        setCategory(currentCategory);
    }

    private void setCategory(String category) {
        currentCategory = category;
        List<Song> visibleSongs = new ArrayList<>();
        if (allActiveSongs != null) {
            for (Song s : allActiveSongs) {
                if (s.getCategory().equalsIgnoreCase(category)) {
                    visibleSongs.add(s);
                }
            }
        }

        // Cập nhật UI
        if (visibleSongs.isEmpty()) {
            if(txtEmpty != null) txtEmpty.setVisibility(View.VISIBLE);
            if(rvGridSongs != null) rvGridSongs.setVisibility(View.GONE);
            if(rvListSongs != null) rvListSongs.setVisibility(View.GONE);
        } else {
            if(txtEmpty != null) txtEmpty.setVisibility(View.GONE);
            if(rvGridSongs != null) rvGridSongs.setVisibility(View.VISIBLE);
            if(rvListSongs != null) rvListSongs.setVisibility(View.VISIBLE);
            
            // Cập nhật dữ liệu cho adapter
            gridAdapter.setSongs(visibleSongs);
            listAdapter.setSongs(visibleSongs);
        }
        // TODO: Cập nhật UI cho nút category được chọn (ví dụ đổi màu)
    }

    /**
     * SỬA LỖI Ở ĐÂY: Đảm bảo gửi đi đủ 3 thông tin quan trọng
     */
    @Override
    public void onSongClick(Song song) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("song_res_id", song.getResId());
        intent.putExtra("song_title", song.getTitle());
        intent.putExtra("song_category", song.getCategory());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Song song) {
        Toast.makeText(this, "Favorite feature is not available.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAddToPlaylistClick(Song song) {
        Toast.makeText(this, "Playlist feature is not available.", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("museflow_auth", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
