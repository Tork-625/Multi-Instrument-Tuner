package com.vd.tuner.ui.controllers;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.widget.ProgressBar;

public class ProgressBarController {

    private ProgressBar tuningProgressBarLeft;
    private ProgressBar tuningProgressBarRight;
    private static final int MAX_PROGRESS = 100;

    private float targetProgress = 0f;
    private float currentProgressLeft = 0f;
    private float currentProgressRight = 0f;
    private float currentProgress = 0f;

    private float goodPitchIncreaseRate = 2.5f;
    private float badPitchDecayRate = 0.5f;
    private float overTimePitchDecayRate = 0.4f;

    private static final float ACCEPTABLE_ANGLE_RANGE = 5f;
    private static final float ACCEPTABLE_CENTS_RANGE = 15f;

    private long holdStartTime = 0L;
    private static final long HOLD_DURATION_MS = 1_500L;

    private MediaPlayer successSound;
    private boolean soundPlayed = false;
    private float soundVolume = 1f;

    public interface ProgressCallback {
        void onProgressFull();
        void onProgressBelowFull();
    }

    private ProgressCallback progressCallback;

    public ProgressBarController(MediaPlayer successSound) {
        this.successSound = successSound;
    }

    public void setProgressBars(ProgressBar left, ProgressBar right) {
        this.tuningProgressBarLeft = left;
        this.tuningProgressBarRight = right;
    }

    public void setProgressCallback(ProgressCallback callback) {
        this.progressCallback = callback;
    }

    public void updateProgress(float currentPitch, float targetPitch, float needleAngle) {
        long now = System.currentTimeMillis();

        if (isInHoldState(now)) {
            handleBlinking(now);
            return;
        }

        if (holdStartTime > 0 && now - holdStartTime >= HOLD_DURATION_MS) {
            resetAfterHold();
        }

        updateTargetProgress(currentPitch, targetPitch, needleAngle);
        updateCurrentProgress();
        clampProgress();
        handleFullProgress(now);
        updateProgressBarUI();
    }

    private boolean isInHoldState(long now) {
        return holdStartTime > 0 && now - holdStartTime < HOLD_DURATION_MS;
    }

    private void handleBlinking(long now) {
        long elapsed = now - holdStartTime;
        boolean blinkOn = (elapsed / 300) % 2 == 0;
        int blinkColor = blinkOn ? Color.parseColor("#04d1e9") : Color.TRANSPARENT;

        if (tuningProgressBarLeft != null) {
            tuningProgressBarLeft.post(() -> {
                tuningProgressBarLeft.setProgress(MAX_PROGRESS);
                tuningProgressBarLeft.setProgressTintList(ColorStateList.valueOf(blinkColor));
            });
        }
        if (tuningProgressBarRight != null) {
            tuningProgressBarRight.post(() -> {
                tuningProgressBarRight.setProgress(MAX_PROGRESS);
                tuningProgressBarRight.setProgressTintList(ColorStateList.valueOf(blinkColor));
            });
        }
    }

    private void resetAfterHold() {
        holdStartTime = 0L;

        if (tuningProgressBarLeft != null) {
            tuningProgressBarLeft.post(() -> tuningProgressBarLeft.setProgressTintList(null));
        }
        if (tuningProgressBarRight != null) {
            tuningProgressBarRight.post(() -> tuningProgressBarRight.setProgressTintList(null));
        }
    }

    private void updateTargetProgress(float currentPitch, float targetPitch, float needleAngle) {
        if (currentPitch <= 0) {
            currentProgress = 0f;
            targetProgress = 0;
            return;
        }

        float cents = calculateCents(currentPitch, targetPitch);

        if (Math.abs(cents) <= ACCEPTABLE_CENTS_RANGE && Math.abs(needleAngle) <= ACCEPTABLE_ANGLE_RANGE) {
            currentProgress += 300f;
            currentProgress = Math.min(10000f, currentProgress);
        } else {
            currentProgress = 0f;
        }

        targetProgress = (int) currentProgress;
    }

    private void updateCurrentProgress() {
        if (targetProgress > currentProgressLeft) {
            currentProgressLeft += goodPitchIncreaseRate;
            currentProgressRight += goodPitchIncreaseRate;
        } else {
            currentProgressLeft -= badPitchDecayRate;
            currentProgressRight -= badPitchDecayRate;
        }

        currentProgressLeft -= overTimePitchDecayRate;
        currentProgressRight -= overTimePitchDecayRate;
    }

    private void clampProgress() {
        currentProgressLeft = Math.max(0, Math.min(MAX_PROGRESS, currentProgressLeft));
        currentProgressRight = Math.max(0, Math.min(MAX_PROGRESS, currentProgressRight));
    }

    private void handleFullProgress(long now) {
        if (currentProgressLeft >= MAX_PROGRESS && currentProgressRight >= MAX_PROGRESS) {
            if (!soundPlayed && successSound != null) {
                successSound.setVolume(soundVolume, soundVolume);
                successSound.seekTo(0);
                successSound.start();
                soundPlayed = true;
            }

            if (holdStartTime == 0L) {
                holdStartTime = now;

                if (tuningProgressBarLeft != null) {
                    tuningProgressBarLeft.post(() -> tuningProgressBarLeft.setProgressTintList(
                            ColorStateList.valueOf(Color.parseColor("#04d1e9"))));
                }
                if (tuningProgressBarRight != null) {
                    tuningProgressBarRight.post(() -> tuningProgressBarRight.setProgressTintList(
                            ColorStateList.valueOf(Color.parseColor("#04d1e9"))));
                }

                if (progressCallback != null) {
                    progressCallback.onProgressFull();
                }
            }
        } else {
            if (tuningProgressBarLeft != null) {
                tuningProgressBarLeft.post(() -> tuningProgressBarLeft.setProgressTintList(null));
            }
            if (tuningProgressBarRight != null) {
                tuningProgressBarRight.post(() -> tuningProgressBarRight.setProgressTintList(null));
            }

            soundPlayed = false;

            if (progressCallback != null) {
                progressCallback.onProgressBelowFull();
            }
        }
    }

    private void updateProgressBarUI() {
        if (tuningProgressBarLeft != null) {
            tuningProgressBarLeft.post(() ->
                    tuningProgressBarLeft.setProgress((int) currentProgressLeft)
            );
        }
        if (tuningProgressBarRight != null) {
            tuningProgressBarRight.post(() ->
                    tuningProgressBarRight.setProgress((int) currentProgressRight)
            );
        }
    }

    public void resetProgress() {
        targetProgress = 0;
        currentProgressLeft = 0;
        currentProgressRight = 0;
        currentProgress = 0f;

        if (tuningProgressBarLeft != null) {
            tuningProgressBarLeft.post(() -> tuningProgressBarLeft.setProgress(0));
        }
        if (tuningProgressBarRight != null) {
            tuningProgressBarRight.post(() -> tuningProgressBarRight.setProgress(0));
        }
    }

    private float calculateCents(float detectedFreq, float targetFreq) {
        if (detectedFreq <= 0 || targetFreq <= 0) return 1000;
        return 1200 * (float) Math.log(detectedFreq / targetFreq) / (float) Math.log(2);
    }

    public boolean isHolding() {
        return holdStartTime > 0;
    }

    public long getHoldElapsedTime() {
        if (holdStartTime == 0) return 0;
        return System.currentTimeMillis() - holdStartTime;
    }

    public void release() {
        if (successSound != null) {
            successSound.release();
            successSound = null;
        }
    }
}