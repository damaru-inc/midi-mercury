# midi-mercury
This is a Java application that connects a Solace Router to a midi device.
It can move data in either direction or in both directions at once.

## Command Line Options
* -b binary mode: Sends midi messages as byte arrays instead of Json text strings.
* -f from midi: Indicates that the program will read from a midi device and transmit to Solace
* -h host: The hostname and port of the Solace instance
* -p password: The password of the Solace instance
* -t to midi: Indicates that the program will receive messages from Solace and send them to a midi device.
* -u username: The username of the Solace instance
* -v VPN name: The VPN name of the Solace instance
* -x test mode: If given, runs the Main.testPerf() function rather than Main.run

Example run command:
java -jar build/libs/midi-mercury.jar -h myhost.com:55555 -v default -u myUsername -p myPassword -t

Either the -f or the -t or both must be given. If -f is given, the program will prompt you to select a midi device to read from. If -t is given it will prompt you to select a midi device to write to.

## Using Software Synthesizers

In order to connect to a software synthesizer, you need to use a software MIDI device. If you're using a Mac, run Audio MIDI Setup, open the MIDI Studio window, and you should see something called the IAC Driver.

When you run midi-mercury, the IAC Driver should show up on the list of devices you can select. So select it. On your software instrument, select the IAC Driver as the input MIDI source.

