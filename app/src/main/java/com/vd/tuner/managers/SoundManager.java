package com.vd.tuner.managers;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundManager {

    private final Context context;
    private MediaPlayer mediaPlayer;
    private float volume = 1f;

    public SoundManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void playSound(int resId) {

        stop();

        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> stop());
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
            } catch (Exception ignored) {}
            finally {
                mediaPlayer = null;
            }
        }
    }

    public void releaseMediaPlayer()
    {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
