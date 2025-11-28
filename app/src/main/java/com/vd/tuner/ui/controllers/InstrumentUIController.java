package com.vd.tuner.ui.controllers;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.fragment.app.Fragment;
import com.vd.tuner.R;
import com.vd.tuner.core.MainActivity;
import com.vd.tuner.ui.fragments.BassHeadstockFragment;
import com.vd.tuner.ui.fragments.GuitarHeadstockFragment;
import com.vd.tuner.ui.fragments.UkuleleHeadstockFragment;

public class InstrumentUIController {

    private final MainActivity activity;
    public InstrumentUIController(MainActivity activity) {
        this.activity = activity;
    }

    public void switchInstrumentUI(String instrumentName) {
        View blurOverlay = activity.findViewById(R.id.blur_overlay);

        if (blurOverlay == null) return;

        blurOverlay.setAlpha(0f);
        blurOverlay.setVisibility(View.VISIBLE);
        blurOverlay.animate()
                .alpha(1f)
                .setDuration(800)
                .start();

        Fragment fragment = getInstrumentFragment(instrumentName);

        activity.getSupportFragmentManager()
                .beginTransaction().setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.instrument_container, fragment)
                .commit();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            blurOverlay.animate()
                    .alpha(0f)
                    .setDuration(800)
                    .withEndAction(() -> blurOverlay.setVisibility(View.GONE))
                    .start();
        }, 600);
    }

    public void switchInstrumentWithoutBlur(String instrumentName) {
        Fragment fragment = getInstrumentFragment(instrumentName);

        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.instrument_container, fragment)
                .commitNow();

        View blurOverlay = activity.findViewById(R.id.blur_overlay);
        if (blurOverlay != null) {
            blurOverlay.setVisibility(View.GONE);
            blurOverlay.setAlpha(0f);
        }
    }

    private Fragment getInstrumentFragment(String instrumentName) {
        if(instrumentName.equalsIgnoreCase("guitar")){
            return new GuitarHeadstockFragment();
        }
        else if(instrumentName.equalsIgnoreCase("bass")) {
            return new BassHeadstockFragment();
        }
        else{
            return new UkuleleHeadstockFragment();
        }
    }
}