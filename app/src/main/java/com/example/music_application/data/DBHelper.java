package com.example.music_application.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "museflow.db";
    private static final int DB_VERSION = 8;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_SONGS = "songs";
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

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
                        "role TEXT," +
                        "display_name TEXT)";

        String createSongs =
                "CREATE TABLE " + TABLE_SONGS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT," +
                        "artist TEXT," +
                        "file_path TEXT," +
                        "res_id INTEGER," +
                        "category TEXT," +
                        "cover_path TEXT," +
                        "favorite INTEGER DEFAULT 0," +
                        "last_played INTEGER DEFAULT 0)";

        String createPlaylists =
                "CREATE TABLE " + TABLE_PLAYLISTS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT,"
                        + "user_id INTEGER," +
                        "created_at INTEGER)";

        String createPlaylistSongs =
                "CREATE TABLE " + TABLE_PLAYLIST_SONGS + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "playlist_id INTEGER," +
                        "song_id INTEGER)";

        db.execSQL(createUsers);
        db.execSQL(createSongs);
        db.execSQL(createPlaylists);
        db.execSQL(createPlaylistSongs);

        insertUserSeed(db, "admin", "123456", "admin");
        insertUserSeed(db, "user", "123456", "user");
    }

    private void insertUserSeed(SQLiteDatabase db, String username, String password, String role) {
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("role", role);
        cv.put("display_name", username);
        db.insert(TABLE_USERS, null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // --- USER METHODS ---
    public User getUserById(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, username, password, role, display_name FROM " + TABLE_USERS + " WHERE id = ?", new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            String username = c.getString(1);
            String password = c.getString(2);
            String role = c.getString(3);
            String displayName = c.getString(4);
            c.close();
            return new User(id, username, password, role, displayName);
        }
        c.close();
        return null;
    }

    public int updateUser(long userId, String username, String password, String displayName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("display_name", displayName);
        return db.update(TABLE_USERS, cv, "id=?", new String[]{String.valueOf(userId)});
    }

    public User login(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id, username, password, role, display_name FROM " + TABLE_USERS +
                " WHERE username=? AND password=?";
        Cursor c = db.rawQuery(sql, new String[]{username, password});
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            String u = c.getString(1);
            String p = c.getString(2);
            String role = c.getString(3);
            String displayName = c.getString(4);
            c.close();
            return new User(id, u, p, role, displayName);
        }
        c.close();
        return null;
    }

    public long registerUser(String username, String password, String displayName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("display_name", displayName);
        cv.put("role", "user");
        return db.insert(TABLE_USERS, null, cv);
    }

    // --- SONG METHODS ---
    public Song getSongById(long songId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, title, artist, file_path, res_id, category, cover_path, favorite FROM " + TABLE_SONGS + " WHERE id = ?", new String[]{String.valueOf(songId)});
        if (c.moveToFirst()) {
            long id = c.getLong(0);
            String title = c.getString(1);
            String artist = c.getString(2);
            String filePath = c.getString(3);
            int resId = c.getInt(4);
            String category = c.getString(5);
            String coverPath = c.getString(6);
            boolean fav = c.getInt(7) == 1;
            c.close();
            return new Song(id, title, artist, filePath, resId, fav, coverPath, category);
        }
        c.close();
        return null;
    }

    public Song getSongByResId(int resId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SONGS + " WHERE res_id = ?", new String[]{String.valueOf(resId)});
        if (c.moveToFirst()) {
            long id = c.getLong(c.getColumnIndexOrThrow("id"));
            String title = c.getString(c.getColumnIndexOrThrow("title"));
            String artist = c.getString(c.getColumnIndexOrThrow("artist"));
            String filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
            String category = c.getString(c.getColumnIndexOrThrow("category"));
            String coverPath = c.getString(c.getColumnIndexOrThrow("cover_path"));
            boolean fav = c.getInt(c.getColumnIndexOrThrow("favorite")) == 1;
            c.close();
            return new Song(id, title, artist, filePath, resId, fav, coverPath, category);
        }
        c.close();
        return null;
    }

    public long insertSong(String title, String artist, String filePath, int resId, String category, String coverPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("artist", artist);
        cv.put("file_path", filePath);
        cv.put("res_id", resId);
        cv.put("category", category);
        cv.put("cover_path", coverPath);
        return db.insert(TABLE_SONGS, null, cv);
    }

    public int updateSong(long songId, String title, String artist, String filePath, int resId, String category, boolean favorite, String coverPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("artist", artist);
        cv.put("file_path", filePath);
        cv.put("res_id", resId);
        cv.put("category", category);
        cv.put("favorite", favorite ? 1 : 0);
        cv.put("cover_path", coverPath);
        return db.update(TABLE_SONGS, cv, "id=?", new String[]{String.valueOf(songId)});
    }

    // --- PLAYLIST METHODS ---
    public List<Playlist> getPlaylistsByUser(long userId) {
        List<Playlist> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name FROM " + TABLE_PLAYLISTS + " WHERE user_id = ?", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String name = c.getString(1);
            list.add(new Playlist(id, name));
        }
        c.close();
        return list;
    }

    public long createPlaylist(long userId, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("user_id", userId);
        cv.put("created_at", System.currentTimeMillis());
        return db.insert(TABLE_PLAYLISTS, null, cv);
    }

    public int getPlaylistCountForUser(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLAYLISTS + " WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            int count = c.getInt(0);
            c.close();
            return count;
        }
        c.close();
        return 0;
    }

    public void deletePlaylist(long playlistId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PLAYLIST_SONGS, "playlist_id=?", new String[]{String.valueOf(playlistId)});
        db.delete(TABLE_PLAYLISTS, "id=?", new String[]{String.valueOf(playlistId)});
    }

    public List<Song> getSongsByPlaylist(long playlistId) {
        List<Song> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT s.id, s.title, s.artist, s.file_path, s.res_id, s.category, s.cover_path, s.favorite " +
                "FROM " + TABLE_SONGS + " s INNER JOIN " + TABLE_PLAYLIST_SONGS + " ps " +
                "ON s.id = ps.song_id WHERE ps.playlist_id = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(playlistId)});
        while (c.moveToNext()) {
            long id = c.getLong(0);
            String title = c.getString(1);
            String artist = c.getString(2);
            String filePath = c.getString(3);
            int resId = c.getInt(4);
            String category = c.getString(5);
            String coverPath = c.getString(6);
            boolean fav = c.getInt(7) == 1;
            list.add(new Song(id, title, artist, filePath, resId, fav, coverPath, category));
        }
        c.close();
        return list;
    }

    public void addSongToPlaylist(long playlistId, long songId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("playlist_id", playlistId);
        cv.put("song_id", songId);
        db.insert(TABLE_PLAYLIST_SONGS, null, cv);
    }
}
