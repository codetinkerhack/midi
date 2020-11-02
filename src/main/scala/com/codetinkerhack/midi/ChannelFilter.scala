package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 28/02/2016.
  */
case class ChannelFilter(channel: Int) extends MidiNode {

  override def processMessage(message: Message, send: Message => Unit): Unit = {
    message.get match {
      case m: ShortMessage if m.getChannel != channel => {
        log(s"Filtered out message on channel: ${channel}, message: ${message.toString()}", message)
//        noop
      }

      case _ =>
        log(s"Filter passed through, channel: ${channel}, message: ${message.toString()}", message)
        send(message)
    }
  }
}
