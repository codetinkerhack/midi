package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiParallel(node1: MidiNode, node2: MidiNode) extends MidiNode {

  override def send(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit  = {
    node1.send(message, timeStamp, null)
    node2.send(message, timeStamp, null)
  }

  override def close(): Unit = {
    node1.close()
    node2.close()
  }
}
