package com.vd.tuner.instruments;

import java.util.LinkedHashMap;

public class Guitar extends Instrument {

    public Guitar() {

        tunePitch = new LinkedHashMap<>() {{
            put("Standard",        new float[]{82.41f, 110.00f, 146.83f, 196.00f, 246.94f, 329.63f});
            put("Drop D",          new float[]{73.42f, 110.00f, 146.83f, 196.00f, 246.94f, 329.63f});
            put("D Standard",      new float[]{73.42f, 98.00f, 130.81f, 174.61f, 220.00f, 293.66f});
            put("Half Step Down",  new float[]{77.78f, 103.83f, 138.59f, 185.00f, 233.08f, 311.13f});
            put("Open G",          new float[]{73.42f, 98.00f, 146.83f, 196.00f, 246.94f, 392.00f});
            put("Open D",          new float[]{73.42f, 110.00f, 146.83f, 185.00f, 220.00f, 293.66f});
            put("Open C",          new float[]{65.41f, 98.00f, 130.81f, 196.00f, 261.63f, 329.63f});
            put("Open A",          new float[]{82.41f, 110.00f, 138.59f, 164.81f, 220.00f, 329.63f});
            put("Drop C#",         new float[]{69.30f, 103.83f, 138.59f, 185.00f, 233.08f, 311.13f});
            put("DADGAD",          new float[]{73.42f, 110.00f, 146.83f, 196.00f, 220.00f, 293.66f});
            put("Open E",          new float[]{82.41f, 110.00f, 164.81f, 207.65f, 261.63f, 329.63f});
            put("Drop C",          new float[]{65.41f, 98.00f, 130.81f, 174.61f, 220.00f, 293.66f});
            put("Double Drop D",   new float[]{73.42f, 110.00f, 146.83f, 196.00f, 246.94f, 73.42f});
        }};

        tuneNotes = new LinkedHashMap<>() {{
            put("Standard",        new String[]{"E", "A", "D", "G", "B", "E"});
            put("Drop D",          new String[]{"D", "A", "D", "G", "B", "E"});
            put("D Standard",      new String[]{"D", "G", "C", "F", "A", "D"});
            put("Half Step Down",  new String[]{"Eb", "Ab", "Db", "Gb", "Bb", "Eb"});
            put("Open G",          new String[]{"D", "G", "D", "G", "B", "G"});
            put("Open D",          new String[]{"D", "A", "D", "F#", "A", "D"});
            put("Open C",          new String[]{"C", "G", "C", "G", "C", "E"});
            put("Open A",          new String[]{"E", "A", "C#", "E", "A", "E"});
            put("Drop C#",         new String[]{"C#", "Ab", "Db", "F#", "Bb", "Eb"});
            put("DADGAD",          new String[]{"D", "A", "D", "G", "A", "D"});
            put("Open E",          new String[]{"E", "A", "E", "G#", "B", "E"});
            put("Drop C",          new String[]{"C", "G", "C", "F", "A", "D"});
            put("Double Drop D",   new String[]{"D", "A", "D", "G", "B", "D"});
        }};

        tuneOctaves = new LinkedHashMap<>() {{
            put("Standard",        new String[]{"2", "2", "3", "3", "3", "4"});
            put("Drop D",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("D Standard",      new String[]{"2", "2", "3", "3", "3", "4"});
            put("Half Step Down",  new String[]{"2", "2", "3", "3", "3", "4"});
            put("Open G",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Open D",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Open C",          new String[]{"2", "2", "3", "3", "4", "4"});
            put("Open A",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Drop C#",         new String[]{"2", "2", "3", "3", "3", "4"});
            put("DADGAD",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Open E",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Drop C",          new String[]{"2", "2", "3", "3", "3", "4"});
            put("Double Drop D",   new String[]{"2", "2", "3", "3", "3", "2"});
        }};
    }
}
