# A simple Scala MIDI library

This library originated as a result of experimenting with Music/MIDI using Scala. I was attempting to build new Midi instruments that would require composing various Midi / Non midi instruments and processing MidiMessages / altering it based on various sensors. 
It was fun and hopefully useful for someone :)

This library introduces MidiNode element that can be Receiver and Transmitter at the same time. It is basic building element that allows creating interconnected MidiNodes that transmit Midi messages / filter / create / modify Midi messages.
Library has several concrete implementations of MidiNode:
 * MidiNode - by default it will just pass messages
 * MidiDelay - delays messages by specified miliseconds delay
 * ChordReader - read chords from the keyboard and create meta messages for ChordModifier 
 * ChordModifier - changes Midi notes passed through and transposes from old to new chord (in a way re harmonising sequence)
 * ChannelRouter - re-routse midi sequence from one channel to another
 * ChannelFilter - filters midi messages only to specific channel
 * MidiUtil.debugMidi - util MidiNodes implementation like debug
 * NoopNode - consumes MidiMessages and does nothing else...

Have a look at example folder to see how it can be utilised to build a midi setup like Keytar or Scalalika (Midi instruments implementation).

TODO: Add video 

MidiNodes can be chained and connected from outputs to inputs of other MidiNodes. 
Same output can be connected to several other MidiNodes thus building MidiNodes processing network.

Below example creates node that Transposes any Midi message by one octave up:

    MidiNode (
        (message, timeStamp) => {
    
          import javax.sound.midi.ShortMessage._
    
          message match {
            case Some(m: ShortMessage) if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
             
              var messageList: List[(Option[ShortMessage], Long)] = List()
    
              messageList = (Some(new ShortMessage(m.getCommand, m.getChannel, (m.getData1 + 12), 0)), 0l) :: messageList
    
              messageList
            }
            
            case _ => List((message, timeStamp))
          }
        })

For example assume we have MidiNodes: MidiInput, A, B, C, D, E (implementing various processing for midi messages) MidiOutput

Possible connection could be:
    
    MidiInput.out(0).connect(A)
    
    A.out(0).connect(B.in(0))
    A.out(0).connect(C.in(1))
    A.out(1).connect(D)
    
    B.out(0).connect(E)
    C.out(1).connect(E)
    
    D.out(1).connect(MidiOutput)
    E.out(0).connect(MidiOutput)

Description of the above example:

MidiInput is usually a midi device e.g. keyboard or could be several different midi inputs from different keyboards e.g. MidiInput1, 2, 3
MidiOutput is usually a midi sound module

Connected Input device output 0 to node A

Midi messages passed through A 
 that was output from node A channel 0 will be delivered to node B channel 0 and node C channel 1
 that was output from node A channel 1 will be delivered to node D

Midi messages passed through B
 that was output from node B channel 0 will be delivered to node E

Midi messages passed through C
 that was output from node C channel 0 will be delivered to node E

Midi messages passed through D
 that was output from node D channel 1 will be delivered to node Output

Midi messages passed through E
 that was output from node E channel 0 will be delivered to node Output


There is also implementation of various non standard Midi inputs TCP / UDP / COM port based: com.codetinkerhack.midi.server
That allow building custom Midi components using electronics like arduino.

Have fun!... :)