package com.example.museflow.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite: users (login + role), songs (danh sách bài hát).
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "museflow.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_SONGS = "songs";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsers =
                "CREATE TABLE " + TABLE_USERS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT," +
                        "role TEXT)";

        String createSongs =
                "CREATE TABLE " + TABLE_SONGS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "file_path TEXT," +
                        "favorite INTEGER DEFAULT 0," +
                        "last_played INTEGER DEFAULT 0)";

        db.execSQL(createUsers);
        db.execSQL(createSongs);

        // seed user/admin
        insertUserSeed(db, "admin", "123456", "admin");
        insertUserSeed(db, "user", "123456", "user");

        // sample songs (bạn cần tạo file trong res/raw: sample1.mp3, sample2.mp3)
        insertSongSeed(db, "Lofi Chill", "MuseFlow",
                "android.resource://com.example.museflow/raw/sample1");
        insertSongSeed(db, "Summer Vibes", "MuseFlow",
                "android.resource://com.example.museflow/raw/sample2");
    }

    private void insertUserSeed(SQLiteDatabase db, String username, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        db.insert(TABLE_USERS, null, cv);
    }

    private void insertSongSeed(SQLiteDatabase db, String title, String artist, String filePath) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("artist", artist);
        cv.put("file_path", filePath);
        db.insert(TABLE_SONGS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nếu sau này nâng cấp DB_VERSION thì drop table & tạo lại
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        onCreate(db);
    }

    // --------- USERS ---------

    public User login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, username, password, role FROM " + TABLE_USERS +
                " WHERE username=? AND password=?";
        Cursor c = db.rawQuery(sql, new String[]{username, password});
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            String u = c.getString(1);
            String p = c.getString(2);
            String role = c.getString(3);
            c.close();
            return new User(id, u, p, role);
        }
        c.close();
        return null;
    }

    // --------- SONGS ---------

    public List<Song> getAllSongs() {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, title, artist, file_path, favorite FROM " + TABLE_SONGS, null);
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String title = c.getString(1);
            String artist = c.getString(2);
            String filePath = c.getString(3);
            boolean fav = c.getInt(4) == 1;
            list.add(new Song(id, title, artist, filePath, fav));
        }
        c.close();
        return list;
    }

    public List<Song> searchSongs(String keyword) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String like = "%" + keyword + "%";
        Cursor c = db.rawQuery(
                "SELECT id, title, artist, file_path, favorite FROM " + TABLE_SONGS +
                        " WHERE title LIKE ? OR artist LIKE ?",
                new String[]{like, like});
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String title = c.getString(1);
            String artist = c.getString(2);
            String filePath = c.getString(3);
            boolean fav = c.getInt(4) == 1;
            list.add(new Song(id, title, artist, filePath, fav));
        }
        c.close();
        return list;
    }

    public void setFavorite(long songId, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("favorite", favorite ? 1 : 0);
        db.update(TABLE_SONGS, cv, "id=?", new String[]{String.valueOf(songId)});
    }

    public void updateLastPlayed(long songId, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("last_played", timestamp);
        db.update(TABLE_SONGS, cv, "id=?", new String[]{String.valueOf(songId)});
    }
}