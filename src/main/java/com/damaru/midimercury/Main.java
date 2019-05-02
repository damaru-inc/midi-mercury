package com.damaru.midimercury;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

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

        if (from) {
            fromDevice = pickDevice(true);

            Transmitter transmitter = fromDevice.getTransmitter();
            SolacePublisher publisher = new SolacePublisher(cmd);
            MidiReceiver midiReceiver = new MidiReceiver(cmd, publisher);
            fromDevice.open();
            transmitter.setReceiver(midiReceiver);
        }

        if (to) {
            toDevice = pickDevice(false);
            Receiver receiver = toDevice.getReceiver();
            toDevice.open();
            SolaceSubscriber subscriber = new SolaceSubscriber(cmd, receiver);
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
        return ret;
    }

    private static void testPerf(CommandLine cmd) throws Exception {
        SolacePublisher publisher = new SolacePublisher(cmd);

        // int numMessages = 10000;
        int numMessages = 400000;

        MidiReceiver mr = new MidiReceiver(cmd, publisher);

        log("" + (new Date()) + " About to send midi via text.");
        mr.setBinary(false);
        ShortMessage sm = new ShortMessage(128, 2, 3, 4);
        for (int i = 0; i < numMessages; i++) {
            mr.send(sm, 0);
        }
        log("" + (new Date()) + " Finish sending midi.");

        Thread.sleep(5000);

        log("" + (new Date()) + " About to send midi via binary.");
        mr.setBinary(true);
        for (int i = 0; i < numMessages; i++) {
            mr.send(sm, 0);
        }

        log("" + (new Date()) + " Finish sending midi.");

        mr.close();

        // String text = "Support for nested build without a settings file was
        // deprecated and will be removed in Gradle 5.0.";
        // String topic = "a/a";
        //
        // log("" + (new Date()) + " About to send text.");
        // for (int i = 0; i < numMessages; i++) {
        // publisher.sendText(text, topic);
        // }
        // log("" + (new Date()) + " Finish sending text.");
        // byte[] data = text.getBytes();
        //
        // Thread.sleep(5000);
        // log("" + (new Date()) + " About to send bytes.");
        // for (int i = 0; i < numMessages; i++) {
        // publisher.sendBinary(data, topic);
        // }
        //
        // log("" + (new Date()) + " Finish sending text.");

    }
}
