package com.example.music_application.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.firebase.SongRepository;
import com.example.music_application.model.Song;

public class AdminEditSongActivity extends AppCompatActivity {

    private EditText edtTitle, edtArtist, edtAudioUrl, edtImageUrl;
    private RadioGroup categoryRadioGroup;
    private RadioButton radioPop, radioIndie, radioHiphop, radioBallad;
    private Button btnSave;
    private ProgressBar progressBar;

    private SongRepository songRepository;
    private Song currentSong;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_song);

        songRepository = new SongRepository();

        initViews();
        checkMode();
        setupListeners();
    }

    private void initViews() {
        edtTitle = findViewById(R.id.edit_song_title);
        edtArtist = findViewById(R.id.edit_song_artist);
        edtAudioUrl = findViewById(R.id.edit_song_audio_url);
        edtImageUrl = findViewById(R.id.edit_song_image_url);
        categoryRadioGroup = findViewById(R.id.category_radio_group);
        radioPop = findViewById(R.id.radio_pop);
        radioIndie = findViewById(R.id.radio_indie);
        radioHiphop = findViewById(R.id.radio_hiphop);
        radioBallad = findViewById(R.id.radio_ballad);
        btnSave = findViewById(R.id.btn_save_song);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void checkMode() {
        Intent intent = getIntent();
        if (intent.hasExtra("song")) {
            isEditMode = true;
            currentSong = (Song) intent.getSerializableExtra("song");
            edtTitle.setText(currentSong.getTitle());
            edtArtist.setText(currentSong.getArtist());
            edtAudioUrl.setText(currentSong.getUrl()); // Use getUrl for consistency
            edtImageUrl.setText(currentSong.getCover()); // Use getCover for consistency

            // Set the selected category
            String category = currentSong.getCategory();
            if (category != null) {
                if (category.equalsIgnoreCase("POP")) {
                    radioPop.setChecked(true);
                } else if (category.equalsIgnoreCase("INDIE")) {
                    radioIndie.setChecked(true);
                } else if (category.equalsIgnoreCase("HIP-HOP")) {
                    radioHiphop.setChecked(true);
                } else if (category.equalsIgnoreCase("BALLAD")) {
                    radioBallad.setChecked(true);
                }
            }

        } else {
            isEditMode = false;
            currentSong = new Song();
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveSong());
    }

    private void saveSong() {
        String title = edtTitle.getText().toString().trim();
        String artist = edtArtist.getText().toString().trim();
        String audioUrl = edtAudioUrl.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();

        if (title.isEmpty() || artist.isEmpty() || audioUrl.isEmpty() || imageUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int selectedCategoryId = categoryRadioGroup.getCheckedRadioButtonId();
        if (selectedCategoryId == -1) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = findViewById(selectedCategoryId);
        String category = selectedRadioButton.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        currentSong.setTitle(title);
        currentSong.setArtist(artist);
        currentSong.setUrl(audioUrl);
        currentSong.setCover(imageUrl);
        currentSong.setCategory(category);

        if (isEditMode) {
            songRepository.updateSong(currentSong, () -> {
                Toast.makeText(this, "Song updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }, e -> handleDatabaseFailure(e));
        } else {
            songRepository.addSong(currentSong, () -> {
                Toast.makeText(this, "Song added successfully", Toast.LENGTH_SHORT).show();
                finish();
            }, e -> handleDatabaseFailure(e));
        }
    }

    private void handleDatabaseFailure(Exception e) {
        progressBar.setVisibility(View.GONE);
        btnSave.setEnabled(true);
        Toast.makeText(this, "Database operation failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}
