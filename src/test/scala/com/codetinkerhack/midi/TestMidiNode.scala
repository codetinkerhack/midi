package com.codetinkerhack.midi

import scala.collection.immutable.List

class TestMidiNode extends MidiNode {
  var history: List[(MidiMessageContainer, Long)] = List()

  override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit = {
    history = (message, timeStamp) :: history
  }

  def getLastMessage() = {
    if(history.isEmpty)
      null
    else
      history.head
  }
}
