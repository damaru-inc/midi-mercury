package com.damaru.midimercury;

import org.apache.commons.cli.CommandLine;

import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class Solace {
    
    protected JCSMPSession session;

    public Solace(CommandLine cmd) throws Exception {
        final JCSMPProperties properties = new JCSMPProperties();
        properties.setProperty(JCSMPProperties.HOST, cmd.getOptionValue('h'));
        properties.setProperty(JCSMPProperties.USERNAME, cmd.getOptionValue('u'));
        properties.setProperty(JCSMPProperties.VPN_NAME, cmd.getOptionValue('v'));
        properties.setProperty(JCSMPProperties.PASSWORD, cmd.getOptionValue('p'));
        session = JCSMPFactory.onlyInstance().createSession(properties);
        session.connect();
    }
}
