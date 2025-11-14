package com.example.music_application.data;

import android.content.Context;

import com.example.music_application.R;
import com.example.music_application.model.Song;

import java.util.ArrayList;
import java.util.List;

public class SongRepository {

    // 3.1. 10 bài gốc – gán category mặc định
    public static List<Song> getBaseSongs() {
        List<Song> list = new ArrayList<>();

        list.add(new Song("Khó Vẽ Nụ Cười", R.raw.kho_ve_nu_cuoi, false, "Pop"));
        list.add(new Song("Mất Kết Nối", R.raw.mat_ket_noi, false, "Pop"));
        list.add(new Song("Nàng Công Chúa Nhỏ (Interlude)", R.raw.nang_cong_chua_nho_interlude, false, "Indie"));
        list.add(new Song("New Heart", R.raw.new_heart, false, "Indie"));
        list.add(new Song("The Real Slim Shady", R.raw.the_real_slim_shady, false, "Hip-Hop"));
        list.add(new Song("Dù Cho Tận Thế", R.raw.du_cho_tan_the, false, "Ballad"));
        list.add(new Song("Hãy Trao Cho Anh", R.raw.hay_trao_cho_anh, false, "Pop"));
        list.add(new Song("I Really Want To Stay At Your House", R.raw.i_really_want_to_stay_at_your_house, false, "Indie"));
        list.add(new Song("Don't Let Me Down", R.raw.dont_let_me_down, false, "Pop"));
        list.add(new Song("Dancing In The Dark", R.raw.dancing_in_the_dark, false, "Ballad"));

        return list;
    }

    // 3.2. Danh sách bài admin đã bật – đọc title + category từ Pref
    public static List<Song> getActiveSongs(Context context) {
        SongPreferences prefs = new SongPreferences(context);
        List<Song> active = new ArrayList<>();

        for (Song base : getBaseSongs()) {
            int resId = base.getResId();
            if (prefs.isSongEnabled(resId)) {
                String title = prefs.getSongTitle(resId, base.getTitle());
                String category = prefs.getSongCategory(resId, base.getCategory());
                active.add(new Song(title, resId, true, category));
            }
        }
        return active;
    }
}