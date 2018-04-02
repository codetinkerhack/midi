package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelFilter(channel: Int) extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    message match {
      case Some(m: ShortMessage) if m.getChannel == channel => {
        send(message,timeStamp)
      }

      case Some(m: MetaMessage) =>
        send(message, timeStamp)

      case _ =>

    }
  }
}
