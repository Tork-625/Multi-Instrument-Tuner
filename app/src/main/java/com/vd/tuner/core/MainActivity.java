package com.vd.tuner.core;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.vd.tuner.managers.PermissionManager;
import com.vd.tuner.R;
import com.vd.tuner.managers.SoundManager;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_RECORD_AUDIO = 1;
    public static boolean isStartupAnimationPending = true;
    public static AppInitializer appInitializer;
    private PermissionManager permissionManager;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forcing font scale
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.fontScale = 1.0f;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());


        setContentView(R.layout.activity_splash);

        soundManager = new SoundManager(this);

        appInitializer = new AppInitializer(this, soundManager);

        permissionManager = new PermissionManager(
                this,
                PERMISSION_RECORD_AUDIO,
                new PermissionManager.Callback() {
                    @Override
                    public void onGranted() {
                        appInitializer.initialize();
                    }

                    @Override
                    public void onDenied() {
                        Toast.makeText(MainActivity.this,
                                "Microphone permission denied",
                                Toast.LENGTH_SHORT).show();
                        appInitializer.initialize();
                    }
                }
        );

        permissionManager.askMicPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.handleResult(requestCode, grantResults);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (appInitializer != null) {
            soundManager.releaseMediaPlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appInitializer != null) {
            soundManager.releaseMediaPlayer();
        }
    }

    public AppInitializer getController() {
        return appInitializer;
    }
}