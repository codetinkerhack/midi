package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

case class MidiFilter(filter: (MidiMessage) => Boolean ) extends MidiNode {

  override def receive(message: MidiMessage, timeStamp: Long): Unit = {
    if (filter(message)) {
        send(message,timeStamp)
    }
  }
}
