package com.codetinkerhack.midi

import scala.collection.immutable.List

case class MidiChain() extends MidiNode {
  var chain: List[MidiNode] = List()

  override def connect(next: MidiNode): MidiNode = {

    next match {
      case n: MidiChain =>
        chain = n.chain ::: chain
        this

      case n: MidiNode =>
        chain = n :: chain
        this

      case _ => this
    }
  }

  override def send(message: MidiMessageContainer)(chain1: List[MidiNode]): Unit = {
    val chainReversed = chain.reverse
    super.send(message)(chainReversed)
  }

  override def close(): Unit = {
    chain.foreach( _.close() )
  }
}
