package com.vd.tuner.managers;

import com.vd.tuner.instruments.Bass;
import com.vd.tuner.instruments.Guitar;
import com.vd.tuner.instruments.Instrument;
import com.vd.tuner.instruments.Ukulele;

public class InstrumentManager {
    public Instrument currentInstrument;
    private final Instrument[] instruments;

    public InstrumentManager(){
        instruments = new Instrument[3];
        instruments[0] = new Ukulele();
        instruments[1] = new Guitar();
        instruments[2] = new Bass();
    }


    public void updateCurrentInstrument(String instrumentName) {
        String instrument = instrumentName.toLowerCase();

        if (instrument.equals("ukulele")) {
            currentInstrument = instruments[0];
        } else if (instrument.equals("guitar")) {
            currentInstrument = instruments[1];
        } else if (instrument.equals("bass")) {
            currentInstrument = instruments[2];
        }
    }

}
