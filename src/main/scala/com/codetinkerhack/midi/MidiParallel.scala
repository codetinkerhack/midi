package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiParallel(node1: MidiNode, node2: MidiNode) extends MidiNode {

  override def send(message: MidiMessageContainer, chain: List[MidiNode]): Unit  = {
    node1.send(message, null)
    node2.send(message, null)
  }

  override def close(): Unit = {
    node1.close()
    node2.close()
  }
}
