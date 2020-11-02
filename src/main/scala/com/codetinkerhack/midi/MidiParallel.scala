package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiParallel(nodes: MidiNode*) extends MidiNode {

  override def send(message: Message)(chain: List[MidiNode]): Unit  = {
    nodes.foreach(_.send(message)(chain))
  }

  override def close(): Unit = {
    nodes.foreach(_.close())
  }
}
