package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiFilter(filter: (MMessage) => Boolean ) extends MidiNode {

  override def processMessage(message: MMessage, send: MMessage => Unit): Unit = {
    if (filter(message)) {
      send(message)
    }
  }
}
