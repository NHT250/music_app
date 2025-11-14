package com.example.music_application.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_application.R;
import com.example.music_application.data.SongPreferences;
import com.example.music_application.data.SongRepository;
import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity implements AdminSongAdapter.OnSongActionListener {

    private RecyclerView rvAdminSongs;
    private Button btnAddSong;
    private AdminSongAdapter adapter;
    private SongPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        prefs = new SongPreferences(this);

        rvAdminSongs = findViewById(R.id.rvAdminSongs);
        btnAddSong = findViewById(R.id.btnAddSong);

        adapter = new AdminSongAdapter(SongRepository.getActiveSongs(this), this);
        rvAdminSongs.setLayoutManager(new LinearLayoutManager(this));
        rvAdminSongs.setAdapter(adapter);

        btnAddSong.setOnClickListener(v -> showAddSongDialog());
    }

    private void refreshList() {
        adapter.setSongs(SongRepository.getActiveSongs(this));
    }

    private void showAddSongDialog() {
        List<Song> baseSongs = SongRepository.getBaseSongs();
        List<Song> available = new ArrayList<>();

        for (Song s : baseSongs) {
            if (!prefs.isSongEnabled(s.getResId())) {
                available.add(s);
            }
        }

        if (available.isEmpty()) {
            Toast.makeText(this, "Đã dùng hết 10 bài, không còn bài để thêm", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] titles = new String[available.size()];
        for (int i = 0; i < available.size(); i++) {
            titles[i] = available.get(i).getTitle();
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn bài hát để thêm")
                .setItems(titles, (dialog, which) -> {
                    Song chosen = available.get(which);
                    showCategoryDialogForNewSong(chosen);
                })
                .show();
    }

    private void showCategoryDialogForNewSong(Song chosen) {
        final String[] categories = {"Pop", "Indie", "Hip-Hop", "Ballad"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn thể loại cho \"" + chosen.getTitle() + "\"")
                .setItems(categories, (dialog, which) -> {
                    String cat = categories[which];
                    prefs.setSongEnabled(chosen.getResId(), true);
                    prefs.setSongTitle(chosen.getResId(), chosen.getTitle());
                    prefs.setSongCategory(chosen.getResId(), cat);
                    refreshList();
                })
                .show();
    }

    @Override
    public void onEdit(Song song) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(song.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("Đổi tên bài hát")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        prefs.setSongTitle(song.getResId(), newTitle);
                        refreshList();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDelete(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Xoá bài hát")
                .setMessage("Xoá \"" + song.getTitle() + "\" khỏi danh sách ứng dụng?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    prefs.setSongEnabled(song.getResId(), false);
                    prefs.clearSongTitle(song.getResId());
                    prefs.clearSongCategory(song.getResId()); // Also clear category
                    refreshList();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}