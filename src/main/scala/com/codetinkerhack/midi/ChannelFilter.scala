package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelFilter(channel: Int) extends MidiNode {

  override def receive(message: MidiMessage, timeStamp: Long): Unit = {
    message match {
      case m: ShortMessage if m.getChannel == channel => {
        send(message,timeStamp)
      }

      case m: MetaMessage =>
        send(message, timeStamp)

      case _ =>

    }
  }
}
