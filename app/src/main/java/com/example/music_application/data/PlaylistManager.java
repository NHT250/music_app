package com.example.music_application.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.music_application.model.Playlist;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {

    private static final String PREF_NAME = "playlist_prefs";
    private static final String KEY_PLAYLISTS = "playlists";

    private final SharedPreferences prefs;
    private final Gson gson;

    public PlaylistManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Playlist> getAllPlaylists() {
        String json = prefs.getString(KEY_PLAYLISTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Playlist>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void savePlaylists(List<Playlist> playlists) {
        String json = gson.toJson(playlists);
        prefs.edit().putString(KEY_PLAYLISTS, json).apply();
    }

    public void addPlaylist(Playlist playlist) {
        List<Playlist> playlists = getAllPlaylists();
        playlists.add(playlist);
        savePlaylists(playlists);
    }

    public void removePlaylist(String playlistName) {
        List<Playlist> playlists = getAllPlaylists();
        for (int i = 0; i < playlists.size(); i++) {
            if (playlists.get(i).getName().equals(playlistName)) {
                playlists.remove(i);
                break;
            }
        }
        savePlaylists(playlists);
    }

    public Playlist getPlaylistByName(String name) {
        List<Playlist> playlists = getAllPlaylists();
        for (Playlist playlist : playlists) {
            if (playlist.getName().equals(name)) {
                return playlist;
            }
        }
        return null;
    }
}