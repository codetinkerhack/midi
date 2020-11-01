package com.codetinkerhack.midi

import scala.collection.immutable.List

class TestMidiNode extends MidiNode {
  var history: List[MidiMessageContainer] = List()

  override def processMessage(message: MidiMessageContainer, chain: List[MidiNode]): Unit = {
    history = message :: history
  }

  def getLastMessage() = {
    if(history.isEmpty)
      null
    else
      history.head
  }
}
