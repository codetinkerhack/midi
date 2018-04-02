package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, ShortMessage}

/**
  * Created by evgeniys on 9/16/15.
  */
object MidiUtil {

  val debugMidi = MidiNode((message, timeStamp) => {

    import ShortMessage._

    message match {
      case Some(m: ShortMessage) if (m.getCommand == PROGRAM_CHANGE) =>
        println("Programm Change")
        List((message, timeStamp))

      case Some(m: ShortMessage) if (m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF) => {
        // Send on broadcast channel
        println(s"ShortMessage: ${if (m.getCommand == NOTE_ON) "on" else "off"}, channel: ${m.getChannel()}, data1: ${m.getData1}, data2: ${m.getData2}")

        List((message, timeStamp))
      }

      case Some(m: ShortMessage) => {
        // Send on broadcast channel
        println(s"ShortMessage: command: ${m.getCommand}, channel: ${m.getChannel()}, data1: ${m.getData1}, data2: ${m.getData2}")

        List((message, timeStamp))
      }

      case Some(m: MetaMessage) =>
        println(s"MetaMessage: ${m.getData.toString}")
        List((message, timeStamp))

      case _ =>
        println(s"Some other message")
        List((message, timeStamp))
    }
  })

}
