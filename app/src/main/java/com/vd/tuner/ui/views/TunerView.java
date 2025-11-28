package com.vd.tuner.ui.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.vd.tuner.R;
import com.vd.tuner.audio.PitchDetector;
import com.vd.tuner.managers.InstrumentManager;
import com.vd.tuner.managers.AutoDetectionManager;
import com.vd.tuner.ui.controllers.NeedleController;
import com.vd.tuner.ui.controllers.ProgressBarController;
import com.vd.tuner.ui.controllers.TextLabelController;
import com.vd.tuner.ui.controllers.TuningController;

public class TunerView extends View implements Runnable, AutoDetectionManager.AutoDetectionCallback {

    public int screenWidth;
    public int screenHeight;
    public double delta = 0;
    public int FPS = 60;
    public double drawInterval = 1000000000.0 / FPS;
    public int drawcount;
    public long timer;
    public Thread updateThread;
    private final NeedleController needleController;
    private volatile float currentPitch = 0;
    private PitchDetector pitchDetector;
    public boolean autoDetectMode = false;
    private ProgressBarController progressBarController;
    public float targetPitch;
    private float lastValidPitchForDisplay = 0f;
    private long lastValidPitchTimeForDisplay = 0;
    private static final long PITCH_ZERO_DELAY_MS = 2000;
    public ToggleButton[] tuningButtons;
    public String currentTuningName;
    public TextView pitchTextView;
    private boolean startupAnimationCompleted = false;
    private AutoDetectionManager autoDetectionManager;
    private InstrumentManager instrumentManager;
    private TextLabelController textController = new TextLabelController();
    private TuningController tuningController;
    private boolean isPitchDisplayFrozen = false;
    private float frozenDisplayPitch = 0f;

    public TunerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (context instanceof Activity) {
            Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
            screenHeight = size.y;
        }

        setFocusable(true);

        updateThread = new Thread(this);

        MediaPlayer successSound = MediaPlayer.create(context, R.raw.success_sound);
        progressBarController = new ProgressBarController(successSound);

        progressBarController.setProgressCallback(new ProgressBarController.ProgressCallback() {
            @Override
            public void onProgressFull() {
                textController.storeOriginalColorsIfNeeded();
                frozenDisplayPitch = lastValidPitchForDisplay;
                isPitchDisplayFrozen = true;
            }

            @Override
            public void onProgressBelowFull() {
                isPitchDisplayFrozen = false;
                frozenDisplayPitch = 0f;
            }
        });

        needleController = new NeedleController();
        needleController.setAnimationCallback(new NeedleController.AnimationCallback() {
            @Override
            public void onAnimationComplete() {
                startupAnimationCompleted = true;
                startPitchdetectorAndGameLoop();
            }
        });
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long currentTime;
        invalidate();
        while (updateThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;
            if (delta >= 1) {
                update();
                drawcount++;
                delta--;
            }
            if (timer >= 1000000000) {
                drawcount = 0;
                timer = 0;
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void update() {
        long now = System.currentTimeMillis();

        if (progressBarController.isHolding() && progressBarController.getHoldElapsedTime() < 1500) {
            textController.applyBlinking(progressBarController.getHoldElapsedTime());
        }

        if (progressBarController.getHoldElapsedTime() >= 1500 && progressBarController.isHolding()) {
            textController.restoreOriginalColors();
        }

        if (startupAnimationCompleted) {
            if (!isPitchDisplayFrozen) {
                needleController.updateNeedleRotation(currentPitch);
            }
        }

        if (autoDetectMode && currentPitch > 0) {
            if (autoDetectionManager != null && currentTuningName != null) {
                autoDetectionManager.autoDetectClosestString(
                        currentPitch,
                        currentTuningName,
                        targetPitch,
                        instrumentManager,
                        this
                );
            }
        }

        float needleAngle = needleController.getNeedleAngle();
        progressBarController.updateProgress(currentPitch, targetPitch, needleAngle);

        float displayPitch = currentPitch;
        if (currentPitch > 0) {
            lastValidPitchForDisplay = currentPitch;
            lastValidPitchTimeForDisplay = now;
        } else if (now - lastValidPitchTimeForDisplay < PITCH_ZERO_DELAY_MS && lastValidPitchForDisplay > 0) {
            displayPitch = lastValidPitchForDisplay;
        }

        float value = isPitchDisplayFrozen ? frozenDisplayPitch : displayPitch;
        textController.updatePitch(value);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        if (!startupAnimationCompleted) {
            textController.startFadeIn();
            needleController.startNeedleAnimation();
        }
    }

    public void setNeedleView(ImageView view) {
        needleController.setNeedleView(view);

        if (!startupAnimationCompleted) {
            needleController.fadeIn(1800);
        }
    }

    public void startPitchdetectorAndGameLoop() {
        runPitchDetector();
        updateThread.start();
    }

    @Override
    public void onNewTargetPitchDetected(float newTargetPitch) {
        setTargetPitch(newTargetPitch);
    }

    public void setTuningButtons(ToggleButton[] buttons) {
        this.tuningButtons = buttons;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (progressBarController != null) {
            progressBarController.release();
        }

        if (pitchDetector != null) {
            stopPitchDetector();
        }
    }

    public void setProgressBars(ProgressBar left, ProgressBar right) {
        progressBarController.setProgressBars(left, right);
    }

    public void setTargetPitch(float target) {
        targetPitch = target;
        needleController.updatePitchRange(target);
        progressBarController.resetProgress();

        if (tuningController != null && startupAnimationCompleted) {
            tuningController.updateTargetNoteLabels(target);
        }
    }

    private final PitchDetector.PitchDetectionListener pitchListener =
            new PitchDetector.PitchDetectionListener() {
                @Override
                public void onPitchDetected(float pitch, float clarity, float rms) {
                    currentPitch = pitch;
                }
                @Override
                public void onError(String message) {
                }
            };

    public void runPitchDetector() {
        pitchDetector = new PitchDetector(pitchListener);
        pitchDetector.start();
    }

    public void stopPitchDetector() {
        if (pitchDetector != null) {
            pitchDetector.stop();
        }
    }

    public void setTuningController(TuningController tuningController) {
        this.tuningController = tuningController;
    }

    public void setInstrumentManager(InstrumentManager instrumentManager) {
        this.instrumentManager = instrumentManager;
    }

    public void setAutoDetectionManager(AutoDetectionManager autoDetectionManager) {
        this.autoDetectionManager = autoDetectionManager;
    }

    public TextLabelController getTextLabelController() {
        return textController;
    }

    public void setPitchTextView(TextView textView) {
        this.pitchTextView = textView;
    }

    public boolean isStartupAnimationCompleted() {
        return startupAnimationCompleted;
    }
}