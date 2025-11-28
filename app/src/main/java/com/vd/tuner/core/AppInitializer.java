package com.vd.tuner.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;

import com.vd.tuner.managers.PreferenceManager;
import com.vd.tuner.R;
import com.vd.tuner.managers.TuningManager;
import com.vd.tuner.managers.AutoDetectionManager;
import com.vd.tuner.managers.SoundManager;
import com.vd.tuner.managers.InstrumentManager;
import com.vd.tuner.ui.controllers.InstrumentUIController;
import com.vd.tuner.ui.controllers.TuningController;
import com.vd.tuner.ui.views.TunerView;

public class AppInitializer {

    private final MainActivity activity;
    public static TunerView tunerView;

    public int lastSelectedStringIndex = 0;

    private TextView pitchTextView;

    private InstrumentManager instrumentManager;
    private InstrumentUIController instrumentUiController;
    private AutoDetectionManager autoDetectionManager;
    private TuningManager tuningsManager;
    private PreferenceManager preferenceManager;
    private final SoundManager soundManager;

    public TuningController tuningController;

    public AppInitializer(MainActivity activity, SoundManager soundManager) {
        this.activity = activity;
        this.soundManager = soundManager;
    }

    public void initialize() {
        try {
            SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
            String lastInstrument = prefs.getString("last_instrument", "ukulele");
            String lastTuningKey = "last_tuning_" + lastInstrument;
            String lastTuning = prefs.getString(lastTuningKey, "Standard");

            activity.setContentView(R.layout.activity_main);

            setupWindow();

            tunerView = activity.findViewById(R.id.TunerView);

            preferenceManager = PreferenceManager.getInstance(activity);

            instrumentManager = new InstrumentManager();
            instrumentUiController = new InstrumentUIController(activity);
            instrumentManager.updateCurrentInstrument(lastInstrument);
            tunerView.setInstrumentManager(instrumentManager);

            autoDetectionManager = new AutoDetectionManager(activity, instrumentManager);
            autoDetectionManager.setupAutoDetectButton();
            tunerView.setAutoDetectionManager(autoDetectionManager);

            pitchTextView = activity.findViewById(R.id.pitchText);
            tunerView.setPitchTextView(pitchTextView);

            tuningController = new TuningController(activity, tunerView, instrumentManager, tunerView.getTextLabelController());
            tuningController.setAutoDetectionManager(autoDetectionManager);
            tunerView.setTuningController(tuningController);

            tuningsManager = new TuningManager(activity);
            tuningsManager.setInstrumentManager(instrumentManager);
            tuningsManager.setTuningController(tuningController);

            ImageView needleView = activity.findViewById(R.id.needle);
            if (needleView != null) {
                needleView.setRotation(31.45f);
                needleView.setAlpha(0f);
                needleView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        needleView.getViewTreeObserver().removeOnPreDrawListener(this);
                        needleView.setVisibility(View.VISIBLE);
                        tunerView.setNeedleView(needleView);
                        return true;
                    }
                });
            }

            ProgressBar left = activity.findViewById(R.id.tuningProgressBarLeft);
            ProgressBar right = activity.findViewById(R.id.tuningProgressBarRight);

            if (left != null && right != null) {
                tunerView.setProgressBars(left, right);
            }

            if (instrumentManager.currentInstrument.tunePitch.containsKey(lastTuning)) {
                tuningController.initTuningUI(lastTuning);
            } else {
                tuningController.initTuningUI("Standard");
            }

            setupInstrumentSpinner(lastInstrument);

            tuningsManager.setupTuningSpinner(lastTuning);

            instrumentUiController.switchInstrumentWithoutBlur(lastInstrument);

            new Handler(Looper.getMainLooper()).postDelayed(() -> soundManager.playSound(R.raw.startup_sound), 500);

        } catch (Exception e) {}
    }

    private void setupWindow() {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        Window window = activity.getWindow();

        if (window != null) {
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.black));
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void setupInstrumentSpinner(String lastInstrument) {
        Spinner instrumentSpinner = activity.findViewById(R.id.instrument_spinner);
        String[] instruments = {"Ukulele", "Guitar", "Bass"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.item_spinner_selected, instruments) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = convertView != null
                        ? convertView
                        : LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_selected, parent, false);

                TextView textView = view.findViewById(R.id.spinner_text);
                ImageView imageView = view.findViewById(R.id.spinner_icon);

                textView.setText(getItem(position));

                if (getItem(position).equals("Ukulele")) {
                    imageView.setImageResource(R.drawable.icon_ukulele);
                } else if (getItem(position).equals("Guitar")) {
                    imageView.setImageResource(R.drawable.icon_guitar);
                } else if (getItem(position).equals("Bass")) {
                    imageView.setImageResource(R.drawable.icon_bass);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = convertView != null
                        ? convertView
                        : LayoutInflater.from(getContext()).inflate(R.layout.item_spinner_dropdown, parent, false);

                TextView textView = view.findViewById(R.id.spinner_text);
                ImageView imageView = view.findViewById(R.id.spinner_icon);

                textView.setText(getItem(position));

                if (getItem(position).equals("Ukulele")) {
                    imageView.setImageResource(R.drawable.icon_ukulele);
                } else if (getItem(position).equals("Guitar")) {
                    imageView.setImageResource(R.drawable.icon_guitar);
                } else if (getItem(position).equals("Bass")) {
                    imageView.setImageResource(R.drawable.icon_bass);
                }

                return view;
            }
        };

        instrumentSpinner.setAdapter(adapter);

        int selection = 0;
        if (lastInstrument.equals("guitar")) selection = 1;
        else if (lastInstrument.equals("bass")) selection = 2;

        instrumentSpinner.setSelection(selection);

        final boolean[] isInitialSetup = {true};

        instrumentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitialSetup[0]) {
                    isInitialSetup[0] = false;
                    return;
                }

                String selected = instruments[position].toLowerCase();
                SharedPreferences prefs = activity.getSharedPreferences("prefs", Context.MODE_PRIVATE);
                String currentInstrumentName = prefs.getString("last_instrument", "ukulele");

                if (!selected.equals(currentInstrumentName)) {
                    preferenceManager.setLastInstrument(selected);
                    instrumentManager.updateCurrentInstrument(selected);

                    tuningsManager.initializeTuningLists();
                    tuningsManager.setupTuningSpinner(preferenceManager.getLastTuning(selected));
                    instrumentUiController.switchInstrumentUI(selected);
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void setupTuningButtonListeners() {
        if (tuningController != null) {
            tuningController.setupTuningButtonListeners();
        }
    }
}