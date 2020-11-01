package com.codetinkerhack.midi

import javax.sound.midi.{MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 30/08/2016.
  */
case class Transposer(i: Int)  extends MidiNode {

  override def processMessage(message: MidiMessageContainer, send: MidiMessageContainer => Unit): Unit = {
    message.get match {
      case m: ShortMessage => {
        val newMessage = new MidiMessageContainer(new ShortMessage(m.getCommand, m.getChannel, m.getData1+i, m.getData2), 0, message.getChord, timeStamp = 0L)
        send(newMessage)
      }
      case _ => send(message)
    }
  }
}
