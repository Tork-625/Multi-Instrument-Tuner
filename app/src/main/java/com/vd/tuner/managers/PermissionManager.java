package com.vd.tuner.managers;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {

    public interface Callback {
        void onGranted();
        void onDenied();
    }

    private final Activity activity;
    private final Callback callback;
    private final int requestCode;

    public PermissionManager(Activity activity, int requestCode, Callback callback) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.callback = callback;
    }

    public void askMicPermission() {
        if (isGranted()) {
            callback.onGranted();
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    requestCode
            );
        }
    }

    public boolean isGranted() {
        return ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public void handleResult(int reqCode, int[] grantResults) {
        if (reqCode != requestCode) return;

        boolean granted = grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (granted) {
            callback.onGranted();
        } else {
            Toast.makeText(activity, "Microphone permission denied", Toast.LENGTH_SHORT).show();
            callback.onDenied();
        }
    }
}