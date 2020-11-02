package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, ShortMessage}

/**
  * Created by evgeniys on 9/16/15.
  */
object MidiUtil {

  val debugMidi = MidiNode( message => {

    import ShortMessage._

    message.get match {
      case m: ShortMessage if (m.getCommand == PROGRAM_CHANGE) =>
        println("Programm Change")

      case m: ShortMessage if (m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF) => {
        println(s"ShortMessage: ${if (m.getCommand == NOTE_ON) "on" else "off"}, channel: ${m.getChannel()}, data1: ${m.getData1}, data2: ${m.getData2}")
      }

      case m: ShortMessage => {
        println(s"ShortMessage: command: ${m.getCommand}, channel: ${m.getChannel()}, data1: ${m.getData1}, data2: ${m.getData2}")
      }

      case m: MetaMessage =>
        println(s"MetaMessage: ${m.getData.toString}")

      case _ =>
        println(s"Some other message")

    }
    List(message)
  })

}
