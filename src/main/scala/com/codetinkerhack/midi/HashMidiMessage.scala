package com.codetinkerhack.midi

import javax.sound.midi.ShortMessage

/**
  * Created by Evgeniy on 15/09/2014.
  */
class HashMidiMessage(m: ShortMessage) extends ShortMessage {

  private var hash: Int = 0

  try {
    super.setMessage(m.getStatus, m.getData1, m.getData2)
    this.hash = messageHash(m)
  } catch {
    case ex: Exception => ex.printStackTrace()
  }


  override def equals(o: Any): Boolean = {
    //if (this eq o) return true
    if (!(o.isInstanceOf[HashMidiMessage])) return false
    val that = o.asInstanceOf[HashMidiMessage]
    hash == that.hashCode
  }

  override def hashCode(): Int = hash

  private def messageHash(m: ShortMessage): Int = m.getChannel << 8 | m.getData1

  override def clone(): AnyRef = {
    new HashMidiMessage(super.clone().asInstanceOf[ShortMessage])
  }
}
