package com.vd.tuner.instruments;

import java.util.LinkedHashMap;

public class Ukulele extends Instrument {

    public Ukulele(){

        tunePitch = new LinkedHashMap<>() {{
            put("Standard",     new float[]{392.00f, 261.63f, 329.63f, 440.00f});
            put("Low G",        new float[]{196.00f, 261.63f, 329.63f, 440.00f});
            put("D Tuning",     new float[]{440.00f, 293.66f, 369.99f, 493.88f});
            put("Baritone",     new float[]{146.83f, 196.00f, 246.94f, 329.63f});
            put("Slack Key",    new float[]{392.00f, 261.63f, 329.63f, 392.00f});
            put("Slide",        new float[]{392.00f, 261.63f, 329.63f, 466.16f});
            put("Canadian",     new float[]{220.00f, 293.66f, 370.00f, 493.88f});
        }};

        tuneNotes = new LinkedHashMap<>() {{
            put("Standard",     new String[]{"G", "C", "E", "A"});
            put("Low G",        new String[]{"G", "C", "E", "A"});
            put("D Tuning",     new String[]{"A", "D", "F#", "B"});
            put("Baritone",     new String[]{"D", "G", "B", "E"});
            put("Slack Key",    new String[]{"G", "C", "E", "G"});
            put("Slide",        new String[]{"G", "C", "E", "A#"});
            put("Canadian",     new String[]{"A", "D", "F#", "B"});
        }};

        tuneOctaves = new LinkedHashMap<>() {{
            put("Standard",     new String[]{"4", "4", "4", "4"});
            put("Low G",        new String[]{"3", "4", "4", "4"});
            put("D Tuning",     new String[]{"4", "4", "4", "4"});
            put("Baritone",     new String[]{"3", "3", "3", "4"});
            put("Slack Key",    new String[]{"4", "4", "4", "4"});
            put("Slide",        new String[]{"4", "4", "4", "4"});
            put("Canadian",     new String[]{"3", "4", "4", "4"});
        }};


    }
}
