package com.example.music_application.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.example.music_application.model.MusicConstant.MUSIC_ACTION;

public class MusicReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, MusicService.class);
        serviceIntent.putExtra(MUSIC_ACTION, intent.getIntExtra(MUSIC_ACTION, 0));
        context.startService(serviceIntent);
    }
}
