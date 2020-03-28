package com.codetinkerhack.example

import javax.sound.midi._
import com.codetinkerhack.midi._
import javax.sound.midi.ShortMessage._

/**
  * Created by Evgeniy on 9/05/2015.
  */
object Keytar2 extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopback")
    val pianoKeys = mh.getTransmitters.get("KEYBOARD")
    val chordKeys = mh.getTransmitters.get("APC Key 25")

    output.open()
    chordKeys.open()
    pianoKeys.open()

    val outputMidi = MidiNode(output.getReceiver)
    val pianoKeysNode = MidiNode(pianoKeys.getTransmitters.get(0))
    val chordKeysNode = MidiNode(chordKeys.getTransmitters.get(0))

    val chordKeysTransform = MidiNode((message: Option[MidiMessage], timeStamp: Long) => {

      val scale = Array[Int](1, 3, 5, 6,      8, 10, 12, 13,
                                  2, 4, 6, 7,      9, 11, 13, 14,
                                  8, 10, 12, 13,  15, 17, 19, 20,
                                  9, 11, 13, 14,  16, 18, 20, 21,
                                 10, 12, 14, 15,  17, 19, 21, 22)

      message match {
        case Some(m: ShortMessage) if(m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
          var messageList = List[(Option[ShortMessage], Long)]()

          val note = scale(39 - m.getData1) + 35

          messageList = (Some(new ShortMessage(m.getCommand, m.getChannel,  note, m.getData2-10)), 0l) :: messageList

          messageList
        }
        case _ => List((message, timeStamp))
      }
    })

    MidiSequencer("069_6-8_Stick_01.midi")
      .connect(MidiFilter( {
        case Some(m: ShortMessage) if m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON => true
        case _ => false
      }))
      .connect(MidiUtil.debugMidi)
      .connect(outputMidi.in(10))

    pianoKeysNode
      .out(0).connect(ChannelRouter(1))
      .out(1).connect(MidiFilter( {
      case Some(m: ShortMessage) if m.getCommand != ShortMessage.PITCH_BEND || m.getCommand != ShortMessage.CONTROL_CHANGE => true
      case _ => false
    }))
      .out(1).connect(MidiUtil.debugMidi)
      .out(1).connect(outputMidi);

    chordKeysNode
      .out(0).connect(chordKeysTransform)
      .out(0).connect(ChannelRouter(0))
      .out(0).connect(MidiUtil.debugMidi)
      .out(0).connect(outputMidi)

  }

}
