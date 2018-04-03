package com.codetinkerhack.midi

import javax.sound.midi.MidiMessage


/**
  * Created by Evgeniy on 14/06/2014.
  */
class NoopNode extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    //black hole...
  }

}
