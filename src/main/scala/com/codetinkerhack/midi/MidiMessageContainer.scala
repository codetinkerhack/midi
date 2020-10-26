package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 15/09/2014.
  */
class MidiMessageContainer(message: MidiMessage, var depth: Int = 0) {

  private var hash: Int = 0

  try {
    this.hash = messageHash(message)
  } catch {
    case ex: Exception => ex.printStackTrace()
  }

  override def equals(o: Any): Boolean = {
    if (!(o.isInstanceOf[MidiMessageContainer])) return false
    val that = o.asInstanceOf[MidiMessageContainer]
    hash == that.hashCode
  }

  override def hashCode(): Int = hash

  private def messageHash(message: MidiMessage): Int = {
    message match {
      case m: ShortMessage =>
        m.getChannel << 8 | m.getData1

      case _ =>
        message.getMessage.hashCode()
    }
  }
  override def clone(): MidiMessageContainer = {
    message match {
      case m: ShortMessage =>
        new MidiMessageContainer(m.clone().asInstanceOf[ShortMessage], depth)

      case m: MetaMessage =>
        new MidiMessageContainer(m.clone().asInstanceOf[MetaMessage], depth)
    }
  }

  def getType(): String = {
    message match {
      case m: ShortMessage if m.getCommand == ShortMessage.NOTE_ON || m.getCommand == ShortMessage.NOTE_OFF =>
        "NoteOn || NoteOff message"

      case m: ShortMessage if m.getCommand == ShortMessage.CONTROL_CHANGE =>
        "ControlChange message"

      case m: MetaMessage =>
        "MetaMessage"

      case _ =>
        "Other message"
    }
  }

  def getDepth = depth

  def incDepth() = { depth = depth + 1 }

  def get = message


}
