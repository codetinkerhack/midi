package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelRouter(channel: Int) extends MidiNode {

  override def processMessage(message: MidiMessageContainer, chain: List[MidiNode]):  Unit = {
    message.get match {
      case m: ShortMessage => {
        val newMessage = new MidiMessageContainer(new ShortMessage(m.getCommand, channel, m.getData1, m.getData2), message.getDepth, message.getChord, timeStamp = 0L)

        log(s"Routed message to input channel: ${channel}", message)
        send(newMessage, chain)
      }

      case _ =>
        log(s"Input passed through message", message)
        send(message, chain)
    }
  }
}
