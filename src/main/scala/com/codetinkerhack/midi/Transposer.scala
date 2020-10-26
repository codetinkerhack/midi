package com.codetinkerhack.midi

import javax.sound.midi.{MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 30/08/2016.
  */
case class Transposer(i: Int)  extends MidiNode {

  override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit = {
    message.get match {
      case m: ShortMessage => {
        val newMessage = new MidiMessageContainer(new ShortMessage(m.getCommand, m.getChannel, m.getData1+i, m.getData2))

        List((newMessage, timeStamp))
      }
      case _ => List()
    }
  }
}
