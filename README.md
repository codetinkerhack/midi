# A simple Scala MIDI library

This library originated as a result of experimenting with Music/MIDI using Scala.
It was fun and hopefully useful :)

This library introduces MidiNode component that can be Receiver and Transmitter at the same time.
Library has several concrete implementations of MidiNode:
 * MidiFunction - Takes function of Midi note => List of Midi notes
 * MidiDelay
 * ChordTransformer
 * ChordAnalyzer
 * MidiUtil.debugMidi
 * DummyReceiver (just consume MidiMessages and does nothing else)

Have a look at example folder to see how it can be utilised to build a midi setup like Keytar (that consist of two separate midi keyboards).

MidiNodes can be chained and connected to inputs and outputs of other MidiNodes. 
Same output can be connected to several other MidiNodes

Assume we have MidiNodes: Input, A, B, C, D, E, Output

Possible use case would be:
    
    Input.out(0).connect(A)
    
    A.out(0).connect(B)
    A.out(0).connect(C)
    A.out(1).connect(D)
    
    B.out(0).connect(E)
    C.out(0).connect(E)
    
    D.out(1).connect(Output)
    E.out(0).connect(Output)

Description of the above case:

Input is usually a midi device e.g. keyboard
Ouptut is usually a midi sound module

Connected Input device output 0 to node A

Any Midi message proxied or originated in A 
 that was sent to node A channel 0 will be delivered to node B and C
 that was sent to node A channel 1 will be delivered to D only

Any Midi message proxied or originated in B
 that was sent to node B channel 0 will be delivered to node E

Any Midi message proxied or originated in C
 that was sent to node C channel 0 will be delivered to node E

Any Midi message proxied or originated in D
 that was sent to node D channel 1 will be delivered to node Output

Any Midi message proxied or originated in E
 that was sent to node E channel 0 will be delivered to node Output




