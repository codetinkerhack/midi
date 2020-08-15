package com.codetinkerhack.midi

import java.io.{File, IOException}
import javax.sound.midi._

object MidiSequencer {

  var midiSequencer: MidiSequencer = null

  val receiver = new Receiver {
    override def send(m: MidiMessage, timeStamp: Long): Unit = {
      midiSequencer.send(m, timeStamp)
    }

    override def close(): Unit = { }
  }
  def apply(fileName: String): MidiSequencer = {
    midiSequencer = new MidiSequencer()
    val file = new File(getClass().getClassLoader().getResource(fileName).getFile())
    playFile(file, receiver)
    midiSequencer
  }

  def playFile(file: File, receiver: Receiver): Unit = try {
    val sequencer = MidiSystem.getSequencer(false) // Get the default Sequencer
    if (sequencer == null) {
      System.err.println("Sequencer device not supported")
      return
    }
    sequencer.open() // Open device
    // Create sequence, the File must contain MIDI file data.
    val sequence = MidiSystem.getSequence(file)

    sequencer.setSequence(sequence) // load it into sequencer
    sequencer.setLoopCount(1000)
    sequencer.setLoopStartPoint(1)
    sequencer.getTransmitter().setReceiver(receiver)
    sequencer.start() // start the playback
  } catch {
    case ex@(_: MidiUnavailableException | _: InvalidMidiDataException | _: IOException) =>
      ex.printStackTrace()
  }
}

class MidiSequencer extends MidiNode {


  override def close() = {
  }
}