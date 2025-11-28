package com.vd.tuner.managers;

import static com.vd.tuner.core.AppInitializer.tunerView;
import static com.vd.tuner.core.MainActivity.appInitializer;

import android.util.TypedValue;
import android.view.SoundEffectConstants;
import android.view.ViewTreeObserver;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.vd.tuner.R;
import com.vd.tuner.core.MainActivity;

public class AutoDetectionManager {

    public ToggleButton autoDetectionToggleButton;
    private final MainActivity activity;
    private final InstrumentManager instrumentManager;

    public interface AutoDetectionCallback {
        void onNewTargetPitchDetected(float newTargetPitch);
    }

    public AutoDetectionManager(MainActivity activity, InstrumentManager instrumentManager){
        this.activity = activity;
        this.instrumentManager = instrumentManager;
    }

    public void autoDetectClosestString(float currentPitch, String currentTuningName, float targetPitch, InstrumentManager instrumentManager, AutoDetectionCallback callback) {
        if (currentPitch <= 0 || currentTuningName == null) return;

        float[] tuning = instrumentManager.currentInstrument.tunePitch.get(currentTuningName);
        if (tuning == null) return;

        int closestStringIndex = -1;
        float minDistance = Float.MAX_VALUE;

        for (int i = 0; i < tuning.length; i++) {
            float distance = Math.abs(currentPitch - tuning[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestStringIndex = i;
            }
        }

        if (closestStringIndex != -1 && minDistance < 50) {
            float newTargetPitch = tuning[closestStringIndex];

            if (Math.abs(newTargetPitch - targetPitch) > 0.01f) {
                callback.onNewTargetPitchDetected(newTargetPitch);
            }
        }
    }

    public void setupAutoDetectButton() {
        autoDetectionToggleButton = activity.findViewById(R.id.automode);

        if (autoDetectionToggleButton == null) {
            return;
        }

        autoDetectionToggleButton.setOnClickListener(v -> {
            v.playSoundEffect(SoundEffectConstants.CLICK);
            toggleAutoDetectMode();
        });

        autoDetectionToggleButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int width = autoDetectionToggleButton.getWidth();

                if (width > 0) {
                    float textSizePx = width * 0.25f;
                    autoDetectionToggleButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                    autoDetectionToggleButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    public void toggleAutoDetectMode() {
        boolean newState = !tunerView.autoDetectMode;
        tunerView.autoDetectMode = newState;

        autoDetectionToggleButton.setChecked(newState);

        if (newState) {
            for (ToggleButton button : tunerView.tuningButtons) {
                button.setChecked(true);
            }
            handleAutoModeOn();
        } else {
            for (int i = 0; i < tunerView.tuningButtons.length; i++) {
                tunerView.tuningButtons[i].setChecked(i == appInitializer.lastSelectedStringIndex);
            }

            handleAutoModeOff(appInitializer.lastSelectedStringIndex);

            Spinner spinner = activity.findViewById(R.id.tuningSpinner);
            if (spinner != null && spinner.getSelectedItem() != null) {
                String currentTuningName = spinner.getSelectedItem().toString();
                float[] frequencies = instrumentManager.currentInstrument.tunePitch.get(currentTuningName);

                if (frequencies != null && appInitializer.lastSelectedStringIndex < frequencies.length) {
                    tunerView.setTargetPitch(frequencies[appInitializer.lastSelectedStringIndex]);
                }
            }
        }
    }

    public void handleAutoModeOn() {
        tunerView.autoDetectMode = true;

        if (tunerView.tuningButtons != null) {
            for (ToggleButton button : tunerView.tuningButtons) {
                button.setChecked(true);
            }
        }

        if (tunerView.currentTuningName != null) {
            float[] tuning = instrumentManager.currentInstrument.tunePitch.get(tunerView.currentTuningName);
            if (tuning != null && tuning.length > 0) {
                tunerView.setTargetPitch(tuning[0]);
            }
        }
    }

    public void handleAutoModeOff(int selectedStringIndex) {
        tunerView.autoDetectMode = false;

        if (tunerView.tuningButtons != null) {
            if (selectedStringIndex >= 0 && selectedStringIndex < tunerView.tuningButtons.length) {
                for (int i = 0; i < tunerView.tuningButtons.length; i++) {
                    tunerView.tuningButtons[i].setChecked(i == selectedStringIndex);
                }
            } else {
                for (int i = 0; i < tunerView.tuningButtons.length; i++) {
                    tunerView.tuningButtons[i].setChecked(i == 0);
                }
            }
        }
    }
}