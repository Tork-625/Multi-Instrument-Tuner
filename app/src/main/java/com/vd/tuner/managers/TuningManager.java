package com.vd.tuner.managers;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.vd.tuner.R;
import com.vd.tuner.core.MainActivity;
import com.vd.tuner.ui.controllers.TuningController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TuningManager {

    private MainActivity mainActivity;
    private View customDialogView;
    private AlertDialog tuningDialog;

    private List<String> tuningNames;
    private Map<String, Integer> tuningImages;
    private InstrumentManager instrumentManager;
    private TuningController tuningController;

    public TuningManager(MainActivity activity){
        this.mainActivity = activity;
    }

    public void setInstrumentManager(InstrumentManager instrumentManager){
        this.instrumentManager = instrumentManager;
    }

    public void setTuningController(TuningController tuningController) {
        this.tuningController = tuningController;
    }

    public void setupTuningSpinner(String lastTuning) {
        Spinner spinner = mainActivity.findViewById(R.id.tuningSpinner);

        initializeTuningLists();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(mainActivity, android.R.layout.simple_spinner_item, tuningNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        int defaultIndex = tuningNames.indexOf(lastTuning);
        if (defaultIndex >= 0) {
            spinner.setSelection(defaultIndex);
        }

        if (tuningNames.contains(lastTuning)) {
            if (tuningController != null) {
                tuningController.initTuningUI(lastTuning);
            }
        } else {
            if (tuningController != null) {
                tuningController.initTuningUI("Standard");
            }
        }

        prepareDialog(spinner);

        spinner.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.playSoundEffect(SoundEffectConstants.CLICK);
                if (tuningDialog != null) tuningDialog.show();
            }
            return true;
        });
    }

    public void initializeTuningLists() {
        tuningNames = new ArrayList<>();

        if (instrumentManager.currentInstrument.tunePitch != null) {
            tuningNames.addAll(instrumentManager.currentInstrument.tunePitch.keySet());
        } else {
            tuningNames.add("Standard");
        }

        initializeTuningImages();
    }

    private void initializeTuningImages() {
        tuningImages = new HashMap<>();

        SharedPreferences prefs =
                mainActivity.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE);

        String currentInstrumentName = prefs.getString("last_instrument", "ukulele");

        if (currentInstrumentName.equals("ukulele")) {
            tuningImages.put("Standard", R.drawable.ukulele_standard);
            tuningImages.put("D Tuning", R.drawable.ukulele_d_tuning);
            tuningImages.put("Slack Key", R.drawable.ukulele_slack_key);
            tuningImages.put("Low G", R.drawable.ukulele_low_g);
            tuningImages.put("Baritone", R.drawable.ukulele_baritone);
            tuningImages.put("Slide", R.drawable.ukulele_slide);
            tuningImages.put("Canadian", R.drawable.ukulele_canadian);
        } else if (currentInstrumentName.equals("guitar")) {
            tuningImages.put("Standard", R.drawable.guitar_standard);
            tuningImages.put("Drop D", R.drawable.guitar_drop_d);
            tuningImages.put("D Standard", R.drawable.guitar_d_standard);
            tuningImages.put("Half Step Down", R.drawable.guitar_half_step_down);
            tuningImages.put("Open G", R.drawable.guitar_open_g);
            tuningImages.put("Open D", R.drawable.guitar_open_d);
            tuningImages.put("Open C", R.drawable.guitar_open_c);
            tuningImages.put("Open A", R.drawable.guitar_open_a);
            tuningImages.put("Drop C#", R.drawable.guitar_drop_c_sharp);
            tuningImages.put("Drop C", R.drawable.guitar_drop_c);
            tuningImages.put("Double Drop D", R.drawable.guitar_double_drop_d);
            tuningImages.put("DADGAD", R.drawable.guitar_dadgad);
            tuningImages.put("Open E", R.drawable.guitar_open_e);
        } else if (currentInstrumentName.equals("bass")) {
            tuningImages.put("Standard", R.drawable.bass_standard);
            tuningImages.put("BEAD", R.drawable.bass_bead);
            tuningImages.put("Drop D", R.drawable.bass_drop_d);
            tuningImages.put("Half Step Down", R.drawable.bass_half_step_down);
            tuningImages.put("D Standard", R.drawable.bass_d_standard);
            tuningImages.put("Drop C", R.drawable.bass_drop_c);
        }
    }

    private void prepareDialog(Spinner spinner) {
        customDialogView = mainActivity.getLayoutInflater().inflate(R.layout.dialog_tuning_select, null);
        if (customDialogView == null) return;

        ListView tuningList = customDialogView.findViewById(R.id.tuning_list);
        if (tuningList == null) return;

        ArrayAdapter<String> listAdapter =
                new ArrayAdapter<String>(mainActivity, R.layout.item_tuning_list, R.id.tuning_item, tuningNames) {

                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        ViewHolder holder;

                        if (convertView == null) {
                            convertView = mainActivity.getLayoutInflater()
                                    .inflate(R.layout.item_tuning_list, parent, false);

                            holder = new ViewHolder();
                            holder.tuningNameTextView = convertView.findViewById(R.id.tuning_item);
                            holder.tuningImageView = convertView.findViewById(R.id.tuning_icon);

                            convertView.setTag(holder);
                        } else {
                            holder = (ViewHolder) convertView.getTag();
                        }

                        if (position < tuningNames.size()) {
                            String tuningName = tuningNames.get(position);
                            Integer imageResId = tuningImages.get(tuningName);

                            if (holder.tuningNameTextView != null) {
                                holder.tuningNameTextView.setText(tuningName);
                            }

                            if (holder.tuningImageView != null) {
                                if (imageResId != null) {
                                    holder.tuningImageView.setImageResource(imageResId);
                                    holder.tuningImageView.setVisibility(View.VISIBLE);
                                } else {
                                    holder.tuningImageView.setVisibility(View.GONE);
                                }
                            }
                        }

                        return convertView != null ? convertView : new View(mainActivity);
                    }

                    class ViewHolder {
                        TextView tuningNameTextView;
                        ImageView tuningImageView;
                    }
                };

        tuningList.setAdapter(listAdapter);

        tuningDialog = new AlertDialog.Builder(mainActivity)
                .setView(customDialogView)
                .create();

        tuningDialog.setCanceledOnTouchOutside(true);

        tuningList.setOnItemClickListener((parent, view, position, id) -> {
            view.playSoundEffect(SoundEffectConstants.CLICK);

            if (position < tuningNames.size()) {
                String selectedTuning = tuningNames.get(position);
                spinner.setSelection(position);

                if (tuningController != null) {
                    tuningController.changeTuning(selectedTuning);
                }

                saveLastTuning(selectedTuning);
            }

            tuningDialog.dismiss();
        });
    }

    private void saveLastTuning(String tuningName) {
        SharedPreferences prefs = mainActivity.getSharedPreferences("prefs", android.content.Context.MODE_PRIVATE);
        String lastInstrument = prefs.getString("last_instrument", "guitar");
        String lastTuningKey = "last_tuning_" + lastInstrument;

        prefs.edit().putString(lastTuningKey, tuningName).apply();
    }
}