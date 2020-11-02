package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiFilter(filter: (Message) => Boolean ) extends MidiNode {

  override def processMessage(message: Message, send: Message => Unit): Unit = {
    if (filter(message)) {
      send(message)
    }
  }
}
