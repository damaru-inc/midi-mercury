package com.damaru.midimercury;

import java.util.HashMap;

import org.apache.commons.cli.CommandLine;

import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class SolacePublisher extends Solace {

    private XMLMessageProducer producer;
    private TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
    private BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
    private HashMap<String, Topic> topics = new HashMap<>();
    
    public SolacePublisher(CommandLine cmd) throws Exception {
        super(cmd);
        
        producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {

            @Override
            public void responseReceived(String messageID) {
                System.out.println("Producer received response for msg: " + messageID);
            }

            @Override
            public void handleError(String messageID, JCSMPException e, long timestamp) {
                System.out.printf("Producer received error for msg: %s@%s - %s%n",
                                   messageID,timestamp,e);
             }
        });
    }
    
    public void sendText(String text, String topic) throws Exception {
        Topic theTopic = topics.get(topic);
        
        if (theTopic == null) {
            theTopic = JCSMPFactory.onlyInstance().createTopic(topic);
            topics.put(topic, theTopic);
        }
        
        textMessage.setText(text);
        producer.send(textMessage, theTopic);
    }
    
    public void sendBinary(byte[] data, String topic) throws Exception {
        Topic theTopic = topics.get(topic);
        
        if (theTopic == null) {
            theTopic = JCSMPFactory.onlyInstance().createTopic(topic);
            topics.put(topic, theTopic);
        }
        
        bytesMessage.setData(data);
        producer.send(bytesMessage, theTopic);
    }

}
