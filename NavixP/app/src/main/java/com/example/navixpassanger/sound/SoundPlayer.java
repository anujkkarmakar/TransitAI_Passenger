package com.example.navixpassanger.sound;

import android.content.Context;
import android.media.MediaPlayer;

import com.example.navixpassanger.R;

public class SoundPlayer {
    private MediaPlayer mediaPlayer;
    private Context context;

    public SoundPlayer(Context context) {
        this.context = context;
    }

    public void playSuccessSound() {
        try {
            // Release any existing MediaPlayer
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            // Create and configure MediaPlayer
            mediaPlayer = MediaPlayer.create(context, R.raw.success_sound);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();
                mediaPlayer = null;
            });
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}