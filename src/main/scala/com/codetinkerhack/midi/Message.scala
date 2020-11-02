package com.codetinkerhack.midi

import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

/**
  * Created by Evgeniy on 15/09/2014.
  */
class Message(message: MidiMessage, var depth: Int = 0, chord: Chord = Chord.NONE, timeStamp: Long = 0L) {

  private var hash: Int = 0

  try {
    this.hash = messageHash(message)
  } catch {
    case ex: Exception => ex.printStackTrace()
  }

  override def equals(o: Any): Boolean = {
    if (!(o.isInstanceOf[Message])) return false
    val that = o.asInstanceOf[Message]
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
  override def clone(): Message = {
    message match {
      case m: ShortMessage =>
        new Message(m.clone().asInstanceOf[ShortMessage], depth, chord, timeStamp = 0L)

      case m: MetaMessage =>
        new Message(m.clone().asInstanceOf[MetaMessage], depth, chord, timeStamp = 0L)
    }
  }

  def getNoteOff(): Message = {
    message match {
      case m: ShortMessage if m.getCommand == ShortMessage.NOTE_ON =>
        val noteOff = new ShortMessage(ShortMessage.NOTE_OFF, m.getChannel, m.getData1, 0)
        new Message(noteOff, depth, chord, 0L)
      case m: ShortMessage if m.getCommand == ShortMessage.NOTE_OFF =>
        new Message(message, depth, chord, 0L)
    }
  }

  override def toString(): String = {
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

  def getChord = chord

  def getTimeStamp = timeStamp

}
