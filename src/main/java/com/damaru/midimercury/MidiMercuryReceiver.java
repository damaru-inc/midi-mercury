package com.damaru.midimercury;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.apache.commons.cli.CommandLine;

import com.google.gson.Gson;

public class MidiReceiver implements Receiver {

    private Gson gson = new Gson();
    private SolacePublisher publisher;
    private boolean binary;

    public MidiReceiver(CommandLine cmd, SolacePublisher publisher) {
        this.publisher = publisher;
        if (cmd.hasOption('b')) {
            binary = true;
        }
    }
    
    public void setBinary(boolean val) {
        this.binary = val;
    }

    @Override
    public void send(MidiMessage message, long timeStamp) {

        if (message instanceof ShortMessage) {
            ShortMessage sm = (ShortMessage) message;
            int channel = sm.getChannel();
            int command = sm.getCommand();

            if (command == ShortMessage.NOTE_OFF || command == ShortMessage.NOTE_ON) {
                String topic = String.format("midi/0/%d", channel);
                if (binary) {
                    byte[] data = sm.getMessage();
                    try {
                        publisher.sendBinary(data, topic);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    MidiJsonMessage jm = MidiUtil.getMidiJsonMessage(sm);
                    String json = gson.toJson(jm);
                    // log.info("Got a message {} {} {} {} {}",
                    // message.getStatus(), sm.getChannel(), sm.getCommand(),
                    // sm.getData1(), sm.getData2());
                    // log.info("Got a message {} {} {} {}", jm.getStatus(),
                    // jm.getChannel(), jm.getNote(), jm.getVelocity());
                    //Main.log("Got a message " + json);
                    try {
                        publisher.sendText(json, topic);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                Main.log("Got command " + command);
            }
        }

    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

}
