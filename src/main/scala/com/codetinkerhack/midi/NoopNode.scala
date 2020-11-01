package com.codetinkerhack.midi

import javax.sound.midi.MidiMessage

import scala.collection.immutable.List


/**
  * Created by Evgeniy on 14/06/2014.
  */
class NoopNode extends MidiNode {

  override def processMessage(message: MidiMessageContainer, send: MidiMessageContainer => Unit): Unit = {
    //black hole...
  }

}
