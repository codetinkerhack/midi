package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

case class MidiFilter(filter: (Option[MidiMessage]) => Boolean ) extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    if (filter(message)) {
        send(message,timeStamp)
    }
  }
}