package com.codetinkerhack.midi

import javax.sound.midi.{MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 28/02/2016.
  */
class Router(out: Int) extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    message match {
      case Some(m: ShortMessage) => {
        val newMessage = Some(new ShortMessage(m.getCommand, out, m.getData1, m.getData2))

        send(newMessage,timeStamp)
      }
      case _ =>
    }
  }
}
