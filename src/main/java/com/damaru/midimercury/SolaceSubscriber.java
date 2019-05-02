package com.damaru.midimercury;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.apache.commons.cli.CommandLine;

import com.google.gson.Gson;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;

public class SolaceSubscriber extends Solace {
    
    Receiver midiReceiver;

    public SolaceSubscriber(CommandLine cmd, Receiver receiver) throws Exception {
        super(cmd);
        midiReceiver = receiver;
    
        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {
            
            @Override
            public void onReceive(BytesXMLMessage msg) {
                
                ShortMessage sm;
                Gson gson = new Gson();
                
                
                if (msg instanceof BytesMessage) {
                    BytesMessage message = (BytesMessage) msg;
                    byte[] data = message.getData();
                    byte status = data[0];
                    int note = data[1];
                    int velocity = data[2];
                    try {
                        sm = new ShortMessage(status, note, velocity);
                        midiReceiver.send(sm, -1);
                    } catch (InvalidMidiDataException e) {
                        Main.log("Exception receiving message " + e);
                    }
                    
                    //sm = new ShortMessage(data);
                    
                } else if (msg instanceof TextMessage) {
                    TextMessage message = (TextMessage) msg;
                    String json = message.getText();
                    MidiJsonMessage midi = gson.fromJson(json, MidiJsonMessage.class);
                    try {
                        sm = MidiUtil.getShortMessage(midi);
                        midiReceiver.send(sm, -1);
                    } catch (InvalidMidiDataException e) {
                        Main.log("Exception receiving message " + e);
                    }
                }                
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
            }
        });
        
        final Topic topic = JCSMPFactory.onlyInstance().createTopic("midi/>");
        session.addSubscription(topic);
        cons.start();
    }
}
