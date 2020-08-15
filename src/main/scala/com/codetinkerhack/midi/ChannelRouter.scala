package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelRouter(channel: Int) extends MidiNode {

  override def receive(message: MidiMessage, timeStamp: Long): Unit = {
    message match {
      case m: ShortMessage => {
        val newMessage = new ShortMessage(m.getCommand, channel, m.getData1, m.getData2)

        send(newMessage,timeStamp)
      }

      case m: MetaMessage =>
        send(message, timeStamp)

      case _ =>
    }
  }
}
