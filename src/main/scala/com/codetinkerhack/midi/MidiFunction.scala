package com.codetinkerhack.midi

import javax.sound.midi._

/**
  * Created by Evgeniy on 30/08/2016.
  */
class MidiFunction(f: (Option[MidiMessage], Long) => List[(Option[MidiMessage], Long)]) extends MidiNode {

  override def receive(message: Option[MidiMessage], timeStamp: Long) {

    f(message, timeStamp) foreach (m => send(m._1, m._2))

  }

}
