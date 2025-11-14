package com.example.music_application.ui;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.music_application.R;
import com.example.music_application.data.DBHelper;
import com.example.music_application.data.Song;

public class AdminEditSongActivity extends AppCompatActivity {

    private EditText edtTitle, edtArtist, edtResourceName, edtCategory;
    private TextView txtTitleScreen;
    private ImageView imgCover;
    private DBHelper dbHelper;

    private long songId = -1; // -1 = create mode
    private String coverPath = null;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_song);

        dbHelper = new DBHelper(this);

        txtTitleScreen = findViewById(R.id.txtTitleScreen);
        edtTitle = findViewById(R.id.edtTitle);
        edtArtist = findViewById(R.id.edtArtist);
        edtResourceName = findViewById(R.id.edtFilePath); // ID is edtFilePath in your layout
        edtCategory = findViewById(R.id.edtCategory);
        imgCover = findViewById(R.id.imgCover);
        Button btnSave = findViewById(R.id.btnSave);

        edtResourceName.setHint("Resource Name (e.g. kho_ve_nu_cuoi)");

        setupImagePicker();

        songId = getIntent().getLongExtra("song_id", -1);
        if (songId != -1) {
            txtTitleScreen.setText("Edit song");
            loadSongData(songId);
        } else {
            txtTitleScreen.setText("Add song");
        }

        imgCover.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveSong());
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        coverPath = uri.toString();
                        imgCover.setImageURI(uri);
                    }
                }
        );
    }

    private void loadSongData(long songId) {
        Song s = dbHelper.getSongById(songId);
        if (s != null) {
            edtTitle.setText(s.getTitle());
            edtArtist.setText(s.getArtist());
            edtCategory.setText(s.getCategory());

            if (s.getResId() != 0) {
                try {
                    String resourceName = getResources().getResourceEntryName(s.getResId());
                    edtResourceName.setText(resourceName);
                } catch (Exception e) {
                    edtResourceName.setText("");
                }
            }

            coverPath = s.getCoverPath();
            if (coverPath != null && !coverPath.isEmpty()) {
                imgCover.setImageURI(Uri.parse(coverPath));
            }
        }
    }

    private void saveSong() {
        String title = edtTitle.getText().toString().trim();
        String artist = edtArtist.getText().toString().trim();
        String resourceName = edtResourceName.getText().toString().trim();
        String category = edtCategory.getText().toString().trim();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(artist) || TextUtils.isEmpty(resourceName) || TextUtils.isEmpty(category)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int resId = getResources().getIdentifier(resourceName, "raw", getPackageName());

        if (resId == 0) {
            Toast.makeText(this, "Invalid resource name. Check the name in res/raw.", Toast.LENGTH_LONG).show();
            return;
        }

        String filePath = "android.resource://" + getPackageName() + "/" + resId;

        if (songId == -1) { // Create mode
            long id = dbHelper.insertSong(title, artist, filePath, resId, category, coverPath);
            if (id != -1) {
                Toast.makeText(this, "Song added", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add song", Toast.LENGTH_SHORT).show();
            }
        } else { // Edit mode
            int rows = dbHelper.updateSong(songId, title, artist, filePath, resId, category, false, coverPath);
            if (rows > 0) {
                Toast.makeText(this, "Song updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update song", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
