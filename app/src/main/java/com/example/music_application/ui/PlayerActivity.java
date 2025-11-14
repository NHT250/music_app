package com.example.music_application.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.model.Song;
import com.example.music_application.player.MusicService;

public class PlayerActivity extends AppCompatActivity {

    private TextView txtTitle, txtArtist;
    private ImageView btnPlayPause, imgBack, btnPrev, btnNext;

    private Song currentSong;
    private boolean isPlaying = true; // Mặc định là đang phát khi vào màn
    private BroadcastReceiver playbackStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // --- 1. Ánh xạ Views (đảm bảo không null) ---
        setupViews();

        // --- 2. Đọc dữ liệu từ Intent và Kiểm tra ---
        if (!loadDataFromIntent()) {
            // Nếu load dữ liệu thất bại, thoát khỏi màn hình
            return;
        }

        // --- 3. Cập nhật giao diện và bắt đầu chơi nhạc ---
        updateUI(currentSong);
        startMusicService();

        // --- 4. Gán sự kiện và Lắng nghe Service ---
        setupClickListeners();
        setupBroadcastReceiver();
    }

    private void setupViews() {
        txtTitle = findViewById(R.id.txtTitle);
        txtArtist = findViewById(R.id.txtArtist);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        imgBack = findViewById(R.id.imgBack);
    }

    private boolean loadDataFromIntent() {
        Intent intent = getIntent();
        int resId = intent.getIntExtra("song_res_id", -1);
        String title = intent.getStringExtra("song_title");
        String category = intent.getStringExtra("song_category");

        if (resId == -1 || title == null) {
            Toast.makeText(this, "Error: Invalid song data received.", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        // Tạo đối tượng Song ngay từ dữ liệu nhận được, KHÔNG tìm kiếm nữa
        currentSong = new Song(title, resId, true, category);
        return true;
    }

    private void updateUI(Song song) {
        txtTitle.setText(song.getTitle());
        txtArtist.setText(song.getCategory()); // Hiển thị Category ở vị trí Artist
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_START);
        intent.putExtra(MusicService.EXTRA_SONG_ID, currentSong.getResId());
        intent.putExtra(MusicService.EXTRA_TITLE, currentSong.getTitle());
        startService(intent);
    }

    private void setupClickListeners() {
        imgBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> sendActionToService(MusicService.ACTION_PLAY_PAUSE));
        btnPrev.setOnClickListener(v -> sendActionToService(MusicService.ACTION_PREV));
        btnNext.setOnClickListener(v -> sendActionToService(MusicService.ACTION_NEXT));
    }

    private void setupBroadcastReceiver() {
        playbackStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (MusicService.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    isPlaying = intent.getBooleanExtra(MusicService.EXTRA_IS_PLAYING, false);
                    btnPlayPause.setImageResource(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                }
            }
        };
        registerReceiver(playbackStateReceiver, new IntentFilter(MusicService.ACTION_STATE_CHANGED));
    }

    private void sendActionToService(String action) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(action);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playbackStateReceiver != null) {
            unregisterReceiver(playbackStateReceiver);
        }
    }
}
