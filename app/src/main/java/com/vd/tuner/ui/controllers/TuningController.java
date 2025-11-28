package com.vd.tuner.ui.controllers;

import static com.vd.tuner.core.MainActivity.appInitializer;

import android.view.SoundEffectConstants;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.vd.tuner.R;
import com.vd.tuner.ui.views.TunerView;
import com.vd.tuner.core.MainActivity;
import com.vd.tuner.managers.InstrumentManager;
import com.vd.tuner.managers.AutoDetectionManager;

public class TuningController {

    private final MainActivity activity;
    private final TunerView panel;
    private final InstrumentManager instrumentManager;
    private final TextLabelController textController;
    private AutoDetectionManager autoDetectionManager;

    public ToggleButton[] tuningButtons;

    public TuningController(MainActivity activity, TunerView panel, InstrumentManager instrumentManager, TextLabelController textController) {
        this.activity = activity;
        this.panel = panel;
        this.instrumentManager = instrumentManager;
        this.textController = textController;
    }

    public void setAutoDetectionManager(AutoDetectionManager autoDetectionManager) {
        this.autoDetectionManager = autoDetectionManager;
    }

    public void setTuningButtons(ToggleButton[] buttons) {
        this.tuningButtons = buttons;
        this.panel.setTuningButtons(buttons);
    }

    public void changeTuning(String tuningName) {
        panel.currentTuningName = tuningName;
        float[] freqs = instrumentManager.currentInstrument.tunePitch.get(tuningName);
        String[] notes = instrumentManager.currentInstrument.tuneNotes.get(tuningName);

        if (freqs == null || notes == null || tuningButtons == null) return;

        boolean wasAutoMode = panel.autoDetectMode;

        for (int i = 0; i < Math.min(tuningButtons.length, notes.length); i++) {
            tuningButtons[i].setText(notes[i]);
            tuningButtons[i].setTextOn(notes[i]);
            tuningButtons[i].setTextOff(notes[i]);
        }

        if (wasAutoMode) {
            if (autoDetectionManager != null) {
                autoDetectionManager.handleAutoModeOn();
            }
        } else {
            int targetIndex = appInitializer.lastSelectedStringIndex < freqs.length ? appInitializer.lastSelectedStringIndex : 0;

            if (autoDetectionManager != null) {
                autoDetectionManager.handleAutoModeOff(targetIndex);
            }
            panel.setTargetPitch(freqs[targetIndex]);
        }

        if (freqs.length > 0) {
            if (panel.isStartupAnimationCompleted()) {
                updateTargetNoteLabels(panel.targetPitch);
            }
        }
    }

    public void initTuningUI(String tuningName) {
        changeTuning(tuningName);

        if (!panel.autoDetectMode && tuningButtons != null && tuningButtons.length > 0) {
            int targetIndex = appInitializer.lastSelectedStringIndex < tuningButtons.length ? appInitializer.lastSelectedStringIndex : 0;

            for (int i = 0; i < tuningButtons.length; i++) {
                tuningButtons[i].setChecked(i == targetIndex);
            }
        }
    }

    public void initializeLabelsFromCurrentTuning() {
        if (instrumentManager == null || instrumentManager.currentInstrument == null || panel.currentTuningName == null) return;

        if (!instrumentManager.currentInstrument.tuneNotes.containsKey(panel.currentTuningName)) return;
        if (!instrumentManager.currentInstrument.tuneOctaves.containsKey(panel.currentTuningName)) return;
        if (!instrumentManager.currentInstrument.tunePitch.containsKey(panel.currentTuningName)) return;

        String[] notes = instrumentManager.currentInstrument.tuneNotes.get(panel.currentTuningName);
        String[] octaves = instrumentManager.currentInstrument.tuneOctaves.get(panel.currentTuningName);
        float[] pitches = instrumentManager.currentInstrument.tunePitch.get(panel.currentTuningName);

        if (notes == null || octaves == null || pitches == null ||
                notes.length == 0 || octaves.length == 0 || pitches.length == 0) return;

        textController.initializeLabels(notes[0], octaves[0], pitches[0]);
    }

    public void updateTargetNoteLabels(float targetPitch) {
        if (instrumentManager == null || instrumentManager.currentInstrument == null || panel.currentTuningName == null) return;

        float[] pitches = instrumentManager.currentInstrument.tunePitch.get(panel.currentTuningName);
        String[] notes = instrumentManager.currentInstrument.tuneNotes.get(panel.currentTuningName);
        String[] octaves = instrumentManager.currentInstrument.tuneOctaves.get(panel.currentTuningName);
        if (pitches == null || notes == null || octaves == null) return;

        int idx = 0;
        for (int i = 0; i < pitches.length; i++) {
            if (Math.abs(pitches[i] - targetPitch) < 0.01f) {
                idx = i;
                break;
            }
        }

        textController.updateTargetNoteLabels(notes[idx], octaves[idx], targetPitch);
    }

    public void setupTuningButtonListeners() {
        if (tuningButtons == null) return;

        for (int i = 0; i < tuningButtons.length; i++) {
            final int buttonIndex = i;

            tuningButtons[i].setOnClickListener(v -> {
                v.playSoundEffect(SoundEffectConstants.CLICK);

                ToggleButton clickedButton = (ToggleButton) v;
                appInitializer.lastSelectedStringIndex = buttonIndex;

                if (panel.autoDetectMode && autoDetectionManager != null) {
                    autoDetectionManager.toggleAutoDetectMode();
                }

                for (ToggleButton tb : tuningButtons) {
                    if (tb != clickedButton) tb.setChecked(false);
                }
                clickedButton.setChecked(true);


                Spinner spinner = activity.findViewById(R.id.tuningSpinner);

                if (spinner != null && spinner.getSelectedItem() != null) {
                    String currentTuningName = spinner.getSelectedItem().toString();
                    float[] frequencies = instrumentManager.currentInstrument.tunePitch.get(currentTuningName);

                    if (frequencies != null && buttonIndex < frequencies.length) {
                        panel.setTargetPitch(frequencies[buttonIndex]);
                    }
                }
            });
        }
    }
}