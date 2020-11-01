package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelFilter(channel: Int) extends MidiNode {

  override def processMessage(message: MidiMessageContainer, send: MidiMessageContainer => Unit): Unit = {
    message.get match {
      case m: ShortMessage if m.getChannel != channel => {
        log(s"Filtered out message on channel: ${channel}, message: ${message.getType()}", message)
//        noop
      }

      case _ =>
        log(s"Filter passed through, channel: ${channel}, message: ${message.getType()}", message)
        send(message)
    }
  }
}
