package com.vd.tuner.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;

public class PitchDetector implements Runnable {

    private static final String LOG_TAG = "AndroidPitchDetector";

    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = 4096;
    private static final float MIN_FREQUENCY = 80.0f;
    private static final float MAX_FREQUENCY = 2000.0f;

    private static final float SILENCE_RMS_THRESHOLD = 0.02f;
    private static final float MIN_CONFIDENCE = 0.25f;
    private static final float NSDF_THRESHOLD = 0.75f;
    private static final float YIN_DIFFERENCE_THRESHOLD = 0.25f;

    private static final int HISTORY_SIZE = 7;

    private final float[] pitchHistory = new float[HISTORY_SIZE];
    private final ReentrantLock historyLock = new ReentrantLock();
    private int historyIndex = 0;

    private final PitchDetectionListener listener;

    private AudioRecord audioRecord;
    private volatile boolean isRunning = false;
    private Thread detectionThread;

    private final byte[] audioBytes = new byte[BUFFER_SIZE * 2];
    private final float[] audioBuffer = new float[BUFFER_SIZE];

    private float smoothedPitch = -1f;

    public interface PitchDetectionListener {
        void onPitchDetected(float pitch, float clarity, float rms);
        void onError(String message);
    }

    public PitchDetector(PitchDetectionListener listener) {
        this.listener = listener;
        for (int i = 0; i < HISTORY_SIZE; i++) {
            pitchHistory[i] = -1f;
        }
    }

    public void start() {
        if (isRunning) return;

        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            minBufferSize = SAMPLE_RATE;
        }

        try {
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    Math.max(minBufferSize, BUFFER_SIZE * 2)
            );
        } catch (SecurityException e) {
            if (listener != null) listener.onError("Microphone permission denied");
            return;
        }

        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            if (listener != null) listener.onError("AudioRecord initialization failed");
            return;
        }

        isRunning = true;
        detectionThread = new Thread(this, "AndroidPitchDetectorThread");
        detectionThread.setPriority(Thread.MAX_PRIORITY);
        detectionThread.start();
    }

    public void stop() {
        isRunning = false;
        if (detectionThread != null) {
            detectionThread.interrupt();
            try {
                detectionThread.join();
            } catch (InterruptedException ignored) {
            }
            detectionThread = null;
        }
        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception ignored) {}
            audioRecord = null;
        }
        smoothedPitch = -1f;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
        try {
            audioRecord.startRecording();
            while (isRunning && !Thread.currentThread().isInterrupted()) {
                int bytesRead = audioRecord.read(audioBytes, 0, audioBytes.length);
                if (bytesRead <= 0) {
                    continue;
                }

                int sampleCount = bytesRead / 2;
                float sumSquares = 0f;
                for (int i = 0; i < sampleCount; i++) {
                    short s = (short) ((audioBytes[2*i + 1] << 8) | (audioBytes[2*i] & 0xFF));
                    float normalized = s / 32768f;
                    audioBuffer[i] = normalized;
                    sumSquares += normalized * normalized;
                }
                for (int i = sampleCount; i < BUFFER_SIZE; i++) {
                    audioBuffer[i] = 0f;
                }

                float rms = (float) Math.sqrt(sumSquares / sampleCount);
                if (rms < SILENCE_RMS_THRESHOLD) {
                    notifyListener(0f, 0f, rms);
                    continue;
                }

                applyHannWindow(audioBuffer);

                float[] yinResult = calculateYIN(audioBuffer);
                float tau = yinResult[0];
                float clarity = yinResult[1];

                if (tau > 0 && clarity >= MIN_CONFIDENCE) {
                    float pitch = SAMPLE_RATE / tau;
                    if (pitch >= MIN_FREQUENCY && pitch <= MAX_FREQUENCY) {
                        updateHistory(pitch);

                        float stablePitch = getMedianPitch();
                        if (stablePitch > 0) {
                            smoothedPitch = adaptiveSmooth(stablePitch, clarity);
                            notifyListener(smoothedPitch, clarity, rms);
                        } else {
                            notifyListener(0f, 0f, rms);
                        }
                    } else {
                        notifyListener(0f, 0f, rms);
                    }
                } else {
                    notifyListener(0f, 0f, rms);
                }
            }
        } catch (Exception e) {
            if (listener != null) listener.onError("Pitch detection error: " + e.getMessage());
            Log.e(LOG_TAG, "Pitch detection loop error", e);
        } finally {
            stop();
        }
    }

    private void applyHannWindow(float[] buffer) {
        int length = buffer.length;
        for (int i = 0; i < length; i++) {
            buffer[i] = buffer[i] * (0.5f - 0.5f * (float) Math.cos(2 * Math.PI * i / (length - 1)));
        }
    }

    private float[] calculateYIN(float[] buffer) {
        int length = buffer.length;
        int maxTau = length / 2;

        float[] acf = new float[maxTau];
        float[] squaredSums = new float[maxTau];

        for (int tau = 0; tau < maxTau; tau++) {
            float acfSum = 0f, bufSqSum1 = 0f, bufSqSum2 = 0f;
            for (int i = 0; i < length - tau; i++) {
                float a = buffer[i];
                float b = buffer[i + tau];
                acfSum += a * b;
                bufSqSum1 += a * a;
                bufSqSum2 += b * b;
            }
            acf[tau] = acfSum;
            squaredSums[tau] = bufSqSum1 + bufSqSum2;
        }

        float[] nsdf = new float[maxTau];
        for (int tau = 0; tau < maxTau; tau++) {
            float denom = squaredSums[tau];
            nsdf[tau] = denom > 1e-9f ? (2f * acf[tau]) / denom : 0f;
        }

        int minTau = Math.max(1, (int)(SAMPLE_RATE / MAX_FREQUENCY));
        int maxTauLimit = Math.min(maxTau - 1, (int)(SAMPLE_RATE / MIN_FREQUENCY));

        List<Candidate> candidates = new ArrayList<>();
        for (int tau = minTau; tau < maxTauLimit; tau++) {
            if (nsdf[tau] > NSDF_THRESHOLD && nsdf[tau] > nsdf[tau - 1] && nsdf[tau] >= nsdf[tau + 1]) {
                float interpolatedTau = tau + parabolicOffset(nsdf, tau);
                float clarity = calculateYinClarity(buffer, length, interpolatedTau);
                candidates.add(new Candidate(tau, clarity, interpolatedTau));
            }
        }

        if (candidates.isEmpty()) {
            int bestTau = -1;
            float minVal = Float.MAX_VALUE;
            for (int tau = minTau; tau < maxTauLimit; tau++) {
                float diff = yinDifference(buffer, length, tau);
                if (diff < minVal) {
                    minVal = diff;
                    bestTau = tau;
                }
            }
            if (bestTau == -1 || minVal > YIN_DIFFERENCE_THRESHOLD) return new float[]{0, 0};

            float clarity = 1f - minVal;
            float interpTau = bestTau + parabolicOffsetOverDiff(buffer, length, bestTau);
            return new float[]{interpTau, clarity};
        }

        Candidate best = null;
        for (Candidate c : candidates) {
            if (best == null || c.clarity > best.clarity) best = c;
        }
        if (best == null || best.clarity < MIN_CONFIDENCE) return new float[]{0, 0};

        return new float[]{best.interpTau, best.clarity};
    }

    private static class Candidate {
        final int tau;
        final float clarity;
        final float interpTau;

        Candidate(int tau, float clarity, float interpTau) {
            this.tau = tau; this.clarity = clarity; this.interpTau = interpTau;
        }
    }

    private float parabolicOffset(float[] data, int pos) {
        if (pos <= 0 || pos >= data.length - 1) return 0f;
        float left = data[pos - 1];
        float center = data[pos];
        float right = data[pos + 1];
        float denom = 2f * (2f * center - left - right);
        if (Math.abs(denom) < 1e-10f) return 0f;
        float offset = (right - left) / denom;
        return Math.max(-0.5f, Math.min(0.5f, offset));
    }

    private float calculateYinClarity(float[] buffer, int length, float tau) {
        int center = Math.round(tau);
        int maxTau = length / 2;
        if (center < 1 || center + 1 >= maxTau) return 0f;

        float diffAtTau = yinDifference(buffer, length, center);
        float runningSum = 0f;
        for (int t = 1; t <= center; t++) {
            runningSum += yinDifference(buffer, length, t);
        }
        if (runningSum == 0f) return 1f - diffAtTau;
        return 1f - (diffAtTau / (runningSum / center));
    }

    private float yinDifference(float[] buffer, int length, int tau) {
        float sum = 0f;
        for (int i = 0; i < length - tau; i++) {
            float delta = buffer[i] - buffer[i + tau];
            sum += delta * delta;
        }
        return sum;
    }

    private float parabolicOffsetOverDiff(float[] buffer, int length, int tau) {
        int maxTau = length / 2;
        if (tau <= 0 || tau >= maxTau - 1) return 0f;

        float y1 = yinDifference(buffer, length, tau - 1);
        float y2 = yinDifference(buffer, length, tau);
        float y3 = yinDifference(buffer, length, tau + 1);

        float denom = 2f * (2f * y2 - y1 - y3);
        if (Math.abs(denom) < 1e-10f) return 0f;

        float offset = (y1 - y3) / denom;
        return Math.max(-0.5f, Math.min(0.5f, offset));
    }

    private void updateHistory(float pitch) {
        historyLock.lock();
        try {
            pitchHistory[historyIndex] = pitch;
            historyIndex = (historyIndex + 1) % HISTORY_SIZE;
        } finally {
            historyLock.unlock();
        }
    }

    private float getMedianPitch() {
        historyLock.lock();
        try {
            float[] positives = new float[HISTORY_SIZE];
            int count = 0;
            for (float p : pitchHistory) if (p > 0) positives[count++] = p;

            if (count == 0) return -1f;
            for (int i = 0; i < count - 1; i++) {
                for (int j = 0; j < count - 1 - i; j++) {
                    if (positives[j] > positives[j + 1]) {
                        float temp = positives[j];
                        positives[j] = positives[j + 1];
                        positives[j + 1] = temp;
                    }
                }
            }
            if (count % 2 == 1) {
                return positives[count / 2];
            } else {
                return (positives[count / 2 - 1] + positives[count / 2]) * 0.5f;
            }
        } finally {
            historyLock.unlock();
        }
    }

    private float adaptiveSmooth(float pitch, float clarity) {
        if (smoothedPitch < 0) {
            smoothedPitch = pitch;
            return pitch;
        }
        float alpha = 0.3f + 0.5f * clarity;
        smoothedPitch = alpha * pitch + (1 - alpha) * smoothedPitch;
        return smoothedPitch;
    }

    private void notifyListener(float pitch, float clarity, float rms) {
        if (listener != null) {
            listener.onPitchDetected(pitch, clarity, rms);
        }
    }
}