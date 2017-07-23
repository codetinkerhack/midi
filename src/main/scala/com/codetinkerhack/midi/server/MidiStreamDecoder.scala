package com.codetinkerhack.midi.server

import java.util.ArrayList
import java.util.List
import java.io.IOException
import java.io.InputStream
import javax.sound.midi.InvalidMidiDataException
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiMessage
import javax.sound.midi.ShortMessage
import javax.sound.midi.SysexMessage
//remove if not needed
import scala.collection.JavaConversions._

class MidiStreamDecoder {

  var is: InputStream = null

 // throw new Exception("Please use other constructor that takes InputStream")

  def this(is: InputStream) {
    this()
    this.is = is
  }

  def readMidiMessage(): MidiMessage = {
    var runningStatus = -1
    val click = 0
    var mm: MidiMessage = null
    val sbyte = is.read()
    if (sbyte < 0xf0) {
      var sm: ShortMessage = null
      sbyte match {
        case ShortMessage.NOTE_OFF => 
          sm = new ShortMessage()
          var v = is.read()
          var v0 = is.read()
          sm.setMessage(sbyte, v, v0)
          runningStatus = sbyte

        case ShortMessage.NOTE_ON => 
          sm = new ShortMessage()
          var v1 = is.read()
          var v2 = is.read()
          sm.setMessage(sbyte, v1, v2)
          runningStatus = sbyte

        case ShortMessage.POLY_PRESSURE | ShortMessage.CONTROL_CHANGE | ShortMessage.PITCH_BEND | ShortMessage.SONG_POSITION_POINTER => 
          sm = new ShortMessage()
          sm.setMessage(sbyte, is.read(), is.read())
          runningStatus = sbyte

        case ShortMessage.PROGRAM_CHANGE | ShortMessage.CHANNEL_PRESSURE | ShortMessage.SONG_SELECT | 0xF5 => 
          sm = new ShortMessage()
          sm.setMessage(sbyte, is.read(), 0)
          runningStatus = sbyte

        case ShortMessage.TUNE_REQUEST | ShortMessage.END_OF_EXCLUSIVE | ShortMessage.TIMING_CLOCK | ShortMessage.START | ShortMessage.CONTINUE | ShortMessage.STOP | ShortMessage.ACTIVE_SENSING | ShortMessage.SYSTEM_RESET => 
          sm = new ShortMessage()
          sm.setMessage(sbyte, 0, 0)
          runningStatus = sbyte

        case _ => if (runningStatus != -1) runningStatus & 0xf0 match {
          case ShortMessage.NOTE_OFF => 
            sm = new ShortMessage()
            var v01 = is.read()
            sm.setMessage(runningStatus, sbyte, v01)
            runningStatus = sbyte

          case ShortMessage.NOTE_ON => 
            sm = new ShortMessage()
            var v3 = is.read()
            sm.setMessage(runningStatus, sbyte, v3)
            runningStatus = sbyte

          case ShortMessage.POLY_PRESSURE | ShortMessage.CONTROL_CHANGE | ShortMessage.PITCH_BEND | ShortMessage.SONG_POSITION_POINTER => 
            sm = new ShortMessage()
            sm.setMessage(runningStatus, sbyte, is.read())

          case ShortMessage.PROGRAM_CHANGE | ShortMessage.CHANNEL_PRESSURE | ShortMessage.SONG_SELECT | 0xF5 => 
            sm = new ShortMessage()
            sm.setMessage(runningStatus, sbyte, 0)

          case ShortMessage.TUNE_REQUEST | ShortMessage.END_OF_EXCLUSIVE | ShortMessage.TIMING_CLOCK | ShortMessage.START | ShortMessage.CONTINUE | ShortMessage.STOP | ShortMessage.ACTIVE_SENSING | ShortMessage.SYSTEM_RESET => 
            sm = new ShortMessage()
            sm.setMessage(runningStatus, 0, 0)

          case _ => throw new InvalidMidiDataException("Invalid Short MIDI Event: " + sbyte)
        } else throw new InvalidMidiDataException("Invalid Short MIDI Event: " + sbyte)
      }
      mm = sm
    } else if (sbyte == 0xf0 || sbyte == 0xf7) {
      val sysex = Array.ofDim[Byte](2000)
      val slen = is.read(sysex)
      val sm = new SysexMessage()
      sm.setMessage(sbyte, sysex, slen)
      mm = sm
      runningStatus = -1
    } else if (sbyte == 0xff) {
      val mtype = is.read()
      val meta = Array.ofDim[Byte](2000)
      val mlen = is.read(meta)
      val metam = new MetaMessage()
      metam.setMessage(mtype, meta, mlen)
      mm = metam
      runningStatus = -1
    } else {
      throw new InvalidMidiDataException("Invalid status byte: " + sbyte)
    }
    mm
  }
}
