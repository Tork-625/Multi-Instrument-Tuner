package com.vd.tuner.instruments;

import java.util.Map;

public abstract class Instrument {
    public Map<String, float[]> tunePitch;
    public Map<String, String[]> tuneNotes;
    public Map<String, String[]> tuneOctaves;
}
