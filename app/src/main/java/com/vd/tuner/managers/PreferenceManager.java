package com.vd.tuner.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static PreferenceManager instance;
    private final SharedPreferences preferences;

    private PreferenceManager(Context applicationContext){
        this.preferences = applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    public synchronized static PreferenceManager getInstance(Context context){
        if(instance == null){
            instance = new PreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setLastInstrument(String instrument) {
        preferences.edit().putString("last_instrument", instrument.toLowerCase()).apply();
    }

    public String getLastTuning(String instrument) {
        return preferences.getString("last_tuning_" + instrument.toLowerCase(), "Standard");
    }
}