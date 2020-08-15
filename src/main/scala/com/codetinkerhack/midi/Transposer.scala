package com.codetinkerhack.midi

import javax.sound.midi.{MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 30/08/2016.
  */
case class Transposer(i: Int)  extends MidiNode {

  override def receive(message: MidiMessage, timeStamp: Long): Unit = {
    message match {
      case m: ShortMessage => {
        val newMessage = new ShortMessage(m.getCommand, m.getChannel, m.getData1+i, m.getData2)

        send(newMessage, timeStamp)
      }
      case _ =>
    }
  }
}
