package com.damaru.midimercury;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

import com.google.gson.Gson;

/**
 *
 * @author mdavis
 */
public class MidiUtil {

    private Gson gson = new Gson();
    
    public static MidiJsonMessage getMidiJsonMessage(ShortMessage sm) {
        MidiJsonMessage ret = new MidiJsonMessage();
        ret.setChannel(sm.getChannel());
        ret.setMidiPort(0);
        ret.setNote(sm.getData1());
        ret.setStatus(sm.getStatus());
        ret.setVelocity(sm.getData2());
        return ret;
    }
    
    public static ShortMessage getShortMessage(MidiJsonMessage jm) throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage(jm.getStatus(), jm.getChannel(), jm.getNote(), jm.getVelocity());
        return sm;
    }

    public static MidiDevice getMidiDevice(String name) {
        MidiDevice ret = null;
        try {
            for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
                if (name.equals(info.getName())) {
                    ret = MidiSystem.getMidiDevice(info);
                    Main.log("Found midiDevice " + name);
                    break;
                }
            }
            
            if (ret == null) {
                Main.log("Couldn't find midiDevice " + name);
            }
        } catch (MidiUnavailableException e) {
            Main.log(e.toString());
        }
        return ret;
    }


}
