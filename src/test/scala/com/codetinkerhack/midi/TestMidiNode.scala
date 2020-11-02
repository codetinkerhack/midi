package com.codetinkerhack.midi

import scala.collection.immutable.List

class TestMidiNode extends MidiNode {
  var history: List[Message] = List()

  override def processMessage(message: Message, send: Message => Unit): Unit = {
    history = message :: history
  }

  def getLastMessage() = {
    if(history.isEmpty)
      null
    else
      history.head
  }
}
