package com.vd.tuner.ui.fragments;

import static com.vd.tuner.core.AppInitializer.tunerView;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;
import androidx.fragment.app.Fragment;

import com.vd.tuner.core.AppInitializer;
import com.vd.tuner.R;
import com.vd.tuner.core.MainActivity;
import com.vd.tuner.ui.controllers.TextLabelController;

public class BassHeadstockFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bass_headstock, container, false);

        if (tunerView == null) {
            return view;
        }

        TextView noteLabel = view.findViewById(R.id.noteLabel);
        TextView octaveLabel = view.findViewById(R.id.octaveLabel);
        TextView frequencyLabel = view.findViewById(R.id.frequencyLabel);

        TextLabelController textCtrl = tunerView.getTextLabelController();
        textCtrl.setLabels(noteLabel, octaveLabel, frequencyLabel, tunerView.pitchTextView);

        ToggleButton b1 = view.findViewById(R.id.button1);
        ToggleButton b2 = view.findViewById(R.id.button2);
        ToggleButton b3 = view.findViewById(R.id.button3);
        ToggleButton b4 = view.findViewById(R.id.button4);
        ToggleButton[] buttons = new ToggleButton[]{b1, b2, b3, b4};

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            AppInitializer controller = activity.getController();

            if (controller != null) {
                controller.tuningController.setTuningButtons(buttons);
                controller.tuningController.initTuningUI(tunerView.currentTuningName);
                controller.setupTuningButtonListeners();
            }
        }

        if (MainActivity.isStartupAnimationPending) {
            MainActivity.isStartupAnimationPending = false;
            if (activity != null && activity.getController() != null) {
                activity.getController().tuningController.initializeLabelsFromCurrentTuning();
            }
            noteLabel.setAlpha(0f);
            octaveLabel.setAlpha(0f);
            frequencyLabel.setAlpha(0f);
            textCtrl.startFadeIn();
        }

        applyDynamicLayouts(view);
        return view;
    }


    private void applyDynamicLayouts(View view) {
        View headstockContainer = view.findViewById(R.id.headstockContainer);
        TextView noteLabel = view.findViewById(R.id.noteLabel);
        TextView octaveLabel = view.findViewById(R.id.octaveLabel);
        TextView frequencyLabel = view.findViewById(R.id.frequencyLabel);

        ToggleButton[] tuningButtons = {
                view.findViewById(R.id.button1),
                view.findViewById(R.id.button2),
                view.findViewById(R.id.button3),
                view.findViewById(R.id.button4),
        };

        if (headstockContainer == null) return;

        headstockContainer.post(() -> {
            try {
                int headstockWidth = headstockContainer.getWidth();
                int headstockHeight = headstockContainer.getHeight();

                if (headstockWidth <= 0 || headstockHeight <= 0) return;

                float noteTextSize = headstockHeight * 0.093f;
                float octaveTextSize = headstockHeight * 0.040f;
                float frequencyTextSize = headstockHeight * 0.03f;
                float buttonSize = headstockWidth / 5f;
                float scaledButtonTextSize = buttonSize * 0.45f;

                if (noteLabel != null) noteLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, noteTextSize);
                if (octaveLabel != null) octaveLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, octaveTextSize);
                if (frequencyLabel != null) frequencyLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, frequencyTextSize);

                for (ToggleButton button : tuningButtons) {
                    if (button == null) continue;
                    ViewGroup.LayoutParams params = button.getLayoutParams();
                    if (params != null) {
                        params.width = (int) buttonSize;
                        params.height = (int) buttonSize;
                        button.setLayoutParams(params);
                        button.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledButtonTextSize);
                    }
                }
            } catch (Exception e) {}
        });
    }
}