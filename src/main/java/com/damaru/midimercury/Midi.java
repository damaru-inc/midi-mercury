package com.damaru.midimercury;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;

public class Midi {

    public List<String> getMidiDevices() {
        List<String> ret = new ArrayList<>();
        int i = 1;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            String desc = String.format("%2d: %s", i++, info.getName());
            ret.add(desc);
        }
        return ret;
    }

    public static List<MidiDevice> getMidiReceivers() throws Exception {
        List<MidiDevice> ret = new ArrayList<>();
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            Main.log("checking receiver " + info.getName());

            if (device.getMaxReceivers() != 0 && !info.getName().startsWith("Real Time Sequencer")) {
                ret.add(device);
            }
        }
        return ret;
    }

    public static List<MidiDevice> getMidiTransmitters() throws Exception {
        List<MidiDevice> ret = new ArrayList<>();
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            MidiDevice device = MidiSystem.getMidiDevice(info);
            Main.log("checking transmitter " + info.getName());

            if (device.getMaxTransmitters() != 0 && !info.getName().startsWith("Real Time Sequencer")) {
                ret.add(device);
            }
        }
        return ret;
    }

    public MidiDevice getDevice(String name) throws Exception {
        MidiDevice ret = null;
        for (MidiDevice.Info info : MidiSystem.getMidiDeviceInfo()) {
            if (info.getName().equals(name)) {
                ret = MidiSystem.getMidiDevice(info);
                break;
            }
        }

        return ret;
    }

}
