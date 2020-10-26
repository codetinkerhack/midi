package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiFilter(filter: (MidiMessageContainer) => Boolean ) extends MidiNode {

  override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit = {
    if (filter(message)) {
      send(message, timeStamp, chain)
    }
  }
}
