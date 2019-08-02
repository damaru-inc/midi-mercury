package com.damaru.midimercury;

import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Main {

    public static void log(String msg) {
        System.out.println(msg);
    }

    public static void prompt(String msg) {
        System.out.print(msg + ": ");
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption("b", "binary");
        options.addOption("f", "from midi");
        options.addRequiredOption("h", "host", true, "host");
        options.addRequiredOption("p", "password", true, "password");
        options.addOption("t", "to midi");
        options.addRequiredOption("u", "username", true, "username");
        options.addRequiredOption("v", "vpn", true, "vpn");
        options.addOption("x", "test");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        boolean test = cmd.hasOption('x');

        if (test) {
            testPerf(cmd);
        } else {
            run(cmd);
        }
    }

    private static void run(CommandLine cmd) throws Exception {
        boolean from = cmd.hasOption('f');
        boolean to = cmd.hasOption('t');

        if (!from && !to) {
            from = true;
            to = true;
        }

        log("from " + from + " to " + to + " binary: " + cmd.hasOption('b'));

        MidiDevice fromDevice = null;
        MidiDevice toDevice = null;
        SolacePublisher publisher = null;
        SolaceSubscriber subscriber = null;

        if (from) {
            fromDevice = pickDevice(true);

            Transmitter transmitter = fromDevice.getTransmitter();
            publisher = new SolacePublisher(cmd);
            MidiMercuryReceiver midiMercuryReceiver = new MidiMercuryReceiver(cmd, publisher);
            fromDevice.open();
            transmitter.setReceiver(midiMercuryReceiver);
        }

        if (to) {
            toDevice = pickDevice(false);
            Receiver receiver = toDevice.getReceiver();
            toDevice.open();
            subscriber = new SolaceSubscriber(cmd, receiver);
            
        }
        
        ShutdownHook shutdownHook = new ShutdownHook(publisher, subscriber);
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        // If we have a publisher we don't need the latch.
        if (!from && to) {
            final CountDownLatch latch = new CountDownLatch(1);
            latch.await();
        }
    }

    private static MidiDevice pickDevice(boolean in) throws Exception {
        MidiDevice ret = null;
        String prompt = in ? "Select the midi device to read from" : "Select the midi device to write to";

        int i = 1;
        List<MidiDevice> devices = in ? Midi.getMidiTransmitters() : Midi.getMidiReceivers();

        for (MidiDevice device : devices) {
            MidiDevice.Info info = device.getDeviceInfo();
            String desc = String.format("%2d: %s %s from %s version %s", i++, info.getName(), info.getDescription(),
                    info.getVendor(), info.getVersion());
            log(desc);
        }

        prompt(prompt);
        Scanner scanner = new Scanner(System.in);

        boolean ok = false;
        int num = 0;

        while (!ok) {
            try {
                num = scanner.nextInt();
                if (num < 1 || num >= i) {
                    log("invalid selection: " + num);
                } else {
                    ok = true;
                }
            } catch (Exception e) {
                String input = scanner.next();
                log("Invalid selection: " + input);
            }
        }

        //if (scanner.hasNext()) {
        //    scanner.next();            
        //}

        ret = devices.get(num - 1);
        log("You picked " + ret.getDeviceInfo().getName());
        scanner.close();
        return ret;
    }

    private static void testPerf(CommandLine cmd) throws Exception {
        SolacePublisher publisher = new SolacePublisher(cmd);

        // int numMessages = 10000;
        int numMessages = 1;

        MidiMercuryReceiver midiMercuryReceiver = new MidiMercuryReceiver(cmd, publisher);

        log("" + (new Date()) + " About to send midi via text.");
        midiMercuryReceiver.setBinary(false);
        ShortMessage sm = new ShortMessage(144, 2, 3, 4);
        for (int i = 0; i < numMessages; i++) {
            midiMercuryReceiver.send(sm, 0);
        }
        log("" + (new Date()) + " Finish sending midi.");

        Thread.sleep(5000);

        log("" + (new Date()) + " About to send midi via binary.");
        midiMercuryReceiver.setBinary(true);
        for (int i = 0; i < numMessages; i++) {
            midiMercuryReceiver.send(sm, 0);
        }

        log("" + (new Date()) + " Finish sending midi.");

        midiMercuryReceiver.close();
    }
    
    static class ShutdownHook extends Thread {
        
        private SolacePublisher publisher;
        private SolaceSubscriber subscriber;
        
        public ShutdownHook(SolacePublisher publisher, SolaceSubscriber subscriber) {
            this.publisher = publisher;
            this.subscriber = subscriber;
        }
        
        @Override
        public void run() {
            if (publisher != null) {
                publisher.close();
            }
            
            if (subscriber != null) {
                subscriber.close();
            }
        }
    }
}
