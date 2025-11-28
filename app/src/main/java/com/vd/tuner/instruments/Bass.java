package com.vd.tuner.instruments;

import java.util.LinkedHashMap;

public class Bass extends Instrument {

    public Bass() {

        tunePitch = new LinkedHashMap<>() {{
            put("Standard",         new float[]{41.20f, 55.00f, 73.42f, 98.00f});
            put("Drop D",           new float[]{36.71f, 55.00f, 73.42f, 98.00f});
            put("Half Step Down",   new float[]{38.89f, 51.91f, 69.30f, 92.50f});
            put("D Standard",       new float[]{36.71f, 49.00f, 73.42f, 98.00f});
            put("Drop C",           new float[]{32.70f, 49.00f, 65.41f, 87.31f});
        }};

        tuneNotes = new LinkedHashMap<>() {{
            put("Standard",         new String[]{"E", "A", "D", "G"});
            put("Drop D",           new String[]{"D", "A", "D", "G"});
            put("Half Step Down",   new String[]{"Eb", "Ab", "Db", "Gb"});
            put("D Standard",       new String[]{"D", "G", "C", "F"});
            put("Drop C",           new String[]{"C", "G", "C", "F"});
        }};

        tuneOctaves = new LinkedHashMap<>() {{
            put("Standard",         new String[]{"1", "1", "2", "2"});
            put("Drop D",           new String[]{"1", "1", "2", "2"});
            put("Half Step Down",   new String[]{"1", "1", "2", "2"});
            put("D Standard",       new String[]{"1", "1", "2", "2"});
            put("Drop C",           new String[]{"1", "1", "2", "2"});
        }};
    }
}
