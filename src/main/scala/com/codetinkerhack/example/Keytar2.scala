package com.codetinkerhack.example

import javax.sound.midi._

import com.codetinkerhack.midi._

/**
  * Created by Evgeniy on 9/05/2015.
  */
object Keytar2 extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopMIDI Port")
    val input = mh.getTransmitters.get("microKEY-37")
    val input1 = mh.getTransmitters.get("APC Key 25")

    output.open()
    input1.open()
    input.open()

    val outputMidi = MidiNode(output.getReceiver)
    val inputMidi = MidiNode(input.getTransmitters.get(0))
    val inputMidi1 = MidiNode(input1.getTransmitters.get(0))

    inputMidi.out(0).connect(MidiUtil.debugMidi).out(0).connect(ChannelRouter(1)).out(1).connect(outputMidi);
    inputMidi1.out(0).connect(Transposer(48)).out(0).connect(MidiUtil.debugMidi).out(0).connect(outputMidi)

  }

}
