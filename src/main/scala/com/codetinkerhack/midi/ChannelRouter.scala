package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelRouter(channel: Int) extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    message match {
      case Some(m: ShortMessage) => {
        val newMessage = Some(new ShortMessage(m.getCommand, channel, m.getData1, m.getData2))

        send(newMessage,timeStamp)
      }

      case Some(m: MetaMessage) =>
        send(message, timeStamp)

      case _ =>
    }
  }
}
