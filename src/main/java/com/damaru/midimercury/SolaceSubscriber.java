package com.damaru.midimercury;

import javax.sound.midi.InvalidMidiDataException;
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
    
    private Receiver midiReceiver;
    private Gson gson = new Gson();
    private XMLMessageConsumer consumer;

    // This doesn't work - it seems to hang on the cons.receive() call even when there are messages.
    public void SolaceSubscriberSync(CommandLine cmd, Receiver receiver) throws Exception {
        //super(cmd);
        midiReceiver = receiver;
        XMLMessageConsumer cons = null;
        
        try {
            cons = session.getMessageConsumer((XMLMessageListener) null);
            Main.log("Starting consumer.");
            cons.start();
            BytesXMLMessage msg = cons.receive();
            while (msg != null) {
                Main.log("Got " + msg);
                if (msg instanceof BytesMessage) {
                    BytesMessage message = (BytesMessage) msg;
                    byte[] data = message.getData();
                    
                    if (data.length == 3) {
                    byte status = data[0];
                    int note = data[1];
                    int velocity = data[2];
                    //Main.log("Got bytes.");
                    try {
                        ShortMessage sm = new ShortMessage(status, note, velocity);
                        midiReceiver.send(sm, -1);
                    } catch (InvalidMidiDataException e) {
                        Main.log("Exception receiving message " + e);
                    }
                    } else {
                        String text = new String(data);
                        processText(text);
                    }
                    
                } else if (msg instanceof TextMessage) {
                    TextMessage message = (TextMessage) msg;
                    String json = message.getText();
                    Main.log("Got midi " + json);
                    processText(json);
                }                
                msg = cons.receive();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Main.log("Closing Solace.");
            if (cons != null) {
                cons.close();
            }
            
            if (session != null) {
                session.closeSession();
            }
        }
    
    }
    
    private void processText(String text) {
        MidiJsonMessage midi = gson.fromJson(text, MidiJsonMessage.class);
        try {
            ShortMessage sm = MidiUtil.getShortMessage(midi);
            midiReceiver.send(sm, -1);
        } catch (InvalidMidiDataException e) {
            Main.log("Exception receiving message " + e);
        }
        
    }

    public SolaceSubscriber(CommandLine cmd, Receiver receiver) throws Exception {
        super(cmd);
        midiReceiver = receiver;
    
        consumer = session.getMessageConsumer(new XMLMessageListener() {
            Gson gson = new Gson();
            
            @Override
            public void onReceive(BytesXMLMessage msg) {
          
                //Main.log("got " + msg.getClass());
                
                if (msg instanceof BytesMessage) {
                    BytesMessage message = (BytesMessage) msg;
                    byte[] data = message.getData();
                    
                    if (data.length == 3) {
                    byte status = data[0];
                    int note = data[1];
                    int velocity = data[2];
                    //Main.log("Got bytes.");
                    try {
                        ShortMessage sm = new ShortMessage(status, note, velocity);
                        midiReceiver.send(sm, -1);
                    } catch (InvalidMidiDataException e) {
                        Main.log("Exception receiving message " + e);
                    }
                    } else {
                        String text = new String(data);
                        processText(text);
                    }
                    
                } else if (msg instanceof TextMessage) {
                    TextMessage message = (TextMessage) msg;
                    String json = message.getText();
                    Main.log("Got midi " + json);
                    processText(json);
                }                
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
            }
            
            private void processText(String text) {
                MidiJsonMessage midi = gson.fromJson(text, MidiJsonMessage.class);
                try {
                    ShortMessage sm = MidiUtil.getShortMessage(midi);
                    midiReceiver.send(sm, -1);
                } catch (InvalidMidiDataException e) {
                    Main.log("Exception receiving message " + e);
                }
                
            }
        });
        
        final Topic topic = JCSMPFactory.onlyInstance().createTopic("midi/>");
        session.addSubscription(topic);
        consumer.start();
    }
    
    @Override
    public void close() {
        if (consumer != null) {
            consumer.close();
        }
        
        super.close();
    }
}
