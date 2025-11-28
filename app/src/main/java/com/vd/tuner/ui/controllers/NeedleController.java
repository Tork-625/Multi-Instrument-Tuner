package com.vd.tuner.ui.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

public class NeedleController {

    private ImageView needle;
    private float needleAngle = -31.45f;

    private float minPitch = 85f;
    private float maxPitch = 500f;

    private float lastValidPitch = 0f;
    private long lastValidPitchTime = 0;
    private static final long PITCH_ZERO_DELAY_MS = 2000;

    public interface AnimationCallback {
        void onAnimationComplete();
    }

    private AnimationCallback animationCallback;

    public NeedleController() {
    }

    public void setNeedleView(ImageView view) {
        this.needle = view;
    }

    public void setAnimationCallback(AnimationCallback callback) {
        this.animationCallback = callback;
    }

    public void updatePitchRange(float targetPitch) {
        this.minPitch = targetPitch - 50;
        this.maxPitch = targetPitch + 50;
    }

    public float mapPitchToNeedleAngle(float pitch) {
        float minAngle = -31.45f;
        float maxAngle = 31.45f;
        long currentTime = System.currentTimeMillis();

        if (pitch > 0) {
            lastValidPitch = pitch;
            lastValidPitchTime = currentTime;
        } else if (currentTime - lastValidPitchTime < PITCH_ZERO_DELAY_MS && lastValidPitch > 0) {
            pitch = lastValidPitch;
        }

        pitch = Math.max(minPitch, Math.min(pitch, maxPitch));

        float targetAngle = minAngle + ((pitch - minPitch) / (maxPitch - minPitch)) * (maxAngle - minAngle);

        float angleDiff = Math.abs(targetAngle - needleAngle);
        float lerpFactor = Math.min(0.1f + angleDiff * 0.02f, 0.25f);
        needleAngle += (targetAngle - needleAngle) * lerpFactor;

        return needleAngle;
    }

    public void updateNeedleRotation(float currentPitch) {
        if (needle == null) return;

        needle.post(() -> {
            needle.setPivotX(needle.getWidth() * 0.5f);
            needle.setPivotY(needle.getHeight() * 2.4f);
            needle.setRotation(mapPitchToNeedleAngle(currentPitch));
        });
    }

    public void startNeedleAnimation() {
        if (needle == null) return;

        needle.post(() -> {
            needle.setPivotX(needle.getWidth() * 0.5f);
            needle.setPivotY(needle.getHeight() * 2.4f);

            ObjectAnimator animator = ObjectAnimator.ofFloat(needle, "rotation", 31.45f, -31.45f);
            animator.setDuration(2500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());

            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animationCallback != null) {
                        animationCallback.onAnimationComplete();
                    }
                }
            });

            animator.start();
        });
    }

    public void fadeIn(long duration) {
        if (needle != null) {
            needle.animate().alpha(1f).setDuration(duration).start();
        }
    }

    public float getNeedleAngle() {
        return needleAngle;
    }

    public void reset() {
        needleAngle = -31.45f;
        lastValidPitch = 0f;
        lastValidPitchTime = 0;
    }
}