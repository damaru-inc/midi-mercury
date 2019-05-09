package com.damaru.midimercury;

public class MidiJsonMessage {
    
    private int channel;
    private int midiPort;
    private int note;
    private int command;
    private int velocity;
    
    public int getChannel() {
        return channel;
    }
    public void setChannel(int channel) {
        this.channel = channel;
    }
    public int getMidiPort() {
        return midiPort;
    }
    public void setMidiPort(int midiPort) {
        this.midiPort = midiPort;
    }
    public int getNote() {
        return note;
    }
    public void setNote(int note) {
        this.note = note;
    }
    public int getCommand() {
        return command;
    }
    public void setCommand(int status) {
        this.command = status;
    }
    public int getVelocity() {
        return velocity;
    }
    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }
    
}
