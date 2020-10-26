package com.codetinkerhack.midi

import javax.sound.midi.{MidiMessage, ShortMessage}
import org.scalatest.matchers.{MatchResult, Matcher}

trait CustomMatchers {

  class MidiMessageMatcher(message: MidiMessageContainer) extends Matcher[MidiMessageContainer] {

    def apply(left: MidiMessageContainer) = {

      MatchResult(
        left.get.asInstanceOf[ShortMessage].getCommand == message.get.asInstanceOf[ShortMessage].getCommand
          && left.get.asInstanceOf[ShortMessage].getChannel == message.get.asInstanceOf[ShortMessage].getChannel
          && left.get.asInstanceOf[ShortMessage].getData1 == message.get.asInstanceOf[ShortMessage].getData1
          && left.get.asInstanceOf[ShortMessage].getData2 == message.get.asInstanceOf[ShortMessage].getData2,
        s"""message did not match"""",
        s"""message did match""""
      )
    }
  }

  def matchMessage(message: MidiMessageContainer) = new MidiMessageMatcher(message)
}

// Make them easy to import with:
// import CustomMatchers._
object CustomMatchers extends CustomMatchers