package com.codetinkerhack.example

import com.codetinkerhack.midi._
import javax.sound.midi.ShortMessage._
import javax.sound.midi._

/**
  * Created by Evgeniy on 9/05/2015.
  */
object Sequencer extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopback")

    output.open()

    val outputMidi = MidiNode(output.getReceiver)

    MidiSequencer("069_6-8_Stick_01.midi")
      .connect(MidiFilter( { message =>
            message.get match {
              case m: ShortMessage if m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON => true
              case _ => false
            }
      }))
      .connect(MidiUtil.debugMidi)
      .connect(outputMidi.routeTo(10))

  }

}
