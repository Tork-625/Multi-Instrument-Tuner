package com.vd.tuner.ui.controllers;

import android.graphics.Color;
import android.widget.TextView;
import java.util.Locale;

public class TextLabelController {

    private TextView noteLabel;
    private TextView octaveLabel;
    private TextView frequencyLabel;
    private TextView pitchText;

    private int originalNoteTextColor = 0;
    private int originalOctaveTextColor = 0;
    private int originalFrequencyTextColor = 0;
    private int originalPitchTextColor = 0;

    private void updateTextColor(TextView label, int color) {
        if (label != null) {
            label.post(() -> label.setTextColor(color));
        }
    }

    private void fadeOutThenUpdateText(TextView label, String newText) {
        if (label == null) return;
        label.post(() -> {
            label.animate().alpha(0f).setDuration(100).withEndAction(() -> {
                label.setText(newText);
                label.animate().alpha(1f).setDuration(200).start();
            }).start();
        });
    }

    private void applyFadeInAnimation(TextView label) {
        if (label != null) {
            label.setAlpha(0f);
            label.animate().alpha(1f).setDuration(2000).start();
        }
    }

    public void setLabels(TextView note, TextView octave, TextView freq, TextView pitchText) {
        this.noteLabel = note;
        this.octaveLabel = octave;
        this.frequencyLabel = freq;
        this.pitchText = pitchText;
    }

    public void initializeLabels(String note, String octave, float frequency) {
        if (noteLabel != null) noteLabel.setText(note);
        if (octaveLabel != null) octaveLabel.setText(octave);
        if (frequencyLabel != null) {
            frequencyLabel.setText(String.format(Locale.US, "%.1f Hz", frequency));
        }
    }

    public void updateTargetNoteLabels(String note, String octave, float freq) {
        fadeOutThenUpdateText(noteLabel, note);
        fadeOutThenUpdateText(octaveLabel, octave);
        fadeOutThenUpdateText(frequencyLabel, String.format(Locale.US, "%.1f Hz", freq));
    }

    public void updatePitch(float displayPitch) {
        if (pitchText != null) {
            final String formatted = String.format(Locale.US, "%.1f Hz", displayPitch);
            pitchText.post(() -> pitchText.setText(formatted));
        }
    }

    public void storeOriginalColorsIfNeeded() {
        if (noteLabel != null && originalNoteTextColor == 0) {
            originalNoteTextColor = noteLabel.getCurrentTextColor();
        }

        if (octaveLabel != null && originalOctaveTextColor == 0) {
            originalOctaveTextColor = octaveLabel.getCurrentTextColor();
        }

        if (frequencyLabel != null && originalFrequencyTextColor == 0) {
            originalFrequencyTextColor = frequencyLabel.getCurrentTextColor();
        }

        if (pitchText != null && originalPitchTextColor == 0) {
            originalPitchTextColor = pitchText.getCurrentTextColor();
        }
    }

    public void applyBlinking(long elapsed) {
        boolean blinkOn = (elapsed / 300) % 2 == 0;

        int noteColor = blinkOn ? originalNoteTextColor : Color.TRANSPARENT;
        int octaveColor = blinkOn ? originalOctaveTextColor : Color.TRANSPARENT;
        int freqColor = blinkOn ? originalFrequencyTextColor : Color.TRANSPARENT;
        int pitchColor = blinkOn ? originalPitchTextColor : Color.TRANSPARENT;

        updateTextColor(noteLabel, noteColor);
        updateTextColor(octaveLabel, octaveColor);
        updateTextColor(frequencyLabel, freqColor);
        updateTextColor(pitchText, pitchColor);
    }

    public void restoreOriginalColors() {
        updateTextColor(noteLabel, originalNoteTextColor);
        updateTextColor(octaveLabel, originalOctaveTextColor);
        updateTextColor(frequencyLabel, originalFrequencyTextColor);
        updateTextColor(pitchText, originalPitchTextColor);
    }

    public void startFadeIn() {
        applyFadeInAnimation(noteLabel);
        applyFadeInAnimation(octaveLabel);
        applyFadeInAnimation(frequencyLabel);
    }
}