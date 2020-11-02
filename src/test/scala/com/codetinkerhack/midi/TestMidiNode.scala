package com.codetinkerhack.midi

import scala.collection.immutable.List

class TestMidiNode extends MidiNode {
  var history: List[MMessage] = List()

  override def processMessage(message: MMessage, send: MMessage => Unit): Unit = {
    history = message :: history
  }

  def getLastMessage() = {
    if(history.isEmpty)
      null
    else
      history.head
  }
}
