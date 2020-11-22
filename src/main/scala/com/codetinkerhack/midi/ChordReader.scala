package com.codetinkerhack.midi

import java.util._
import java.util.concurrent.atomic.AtomicReference

import javax.sound.midi._


class ChordReader extends MidiNode {

  private val chordToPlay = new AtomicReference[(Chord, Message => Unit)]()
  private val chordMap = new HashMap[Integer, Chord]()
  private val RELEASED = 0
  private val PRESSED = 1
  private var keysCombo: Int = 0
  private var audibleKeysCombo: Int = 0
  private var timer : Long = 0

  MidiNode.register1MsTimedHandler(this.handler)

  override def getName(): String = "ChordReader"

  override def processMessage(message: Message, send: Message => Unit): Unit = {

    message.get match {
      case m: ShortMessage if (m.getCommand == ShortMessage.NOTE_ON) =>
        keysCombo = getKeysCombo(m.getData1, PRESSED, this.keysCombo)
        if (chordMap.containsKey(keysCombo)) {
          chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo
          chordOn(keysCombo, send)
          val nothing = new String()

          send(new Message(new MetaMessage(1, nothing.getBytes(), nothing.getBytes().length), message.getDepth, Chord.NONE, timeStamp = 0L))
        }

      case m: ShortMessage if (m.getCommand == ShortMessage.NOTE_OFF) =>
        keysCombo = getKeysCombo(m.getData1, RELEASED, this.keysCombo)
        if (chordMap.containsKey(keysCombo) || keysCombo == 0) {
          if (audibleKeysCombo != 0) chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo
          chordOn(keysCombo, send)
          val nothing = new String()
          send(new Message(new MetaMessage(1, nothing.getBytes(), nothing.getBytes().length), message.getDepth,  Chord.NONE, timeStamp = 0L))
        }

      case _ => send(message)

    }

  }

  def addToChordMap(keysCombo: Int, c: Chord) {
    chordMap.put(keysCombo, c)
  }

  private def chordOff(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
      if (chordToPlay.get != null && chordMap.get(keysCombo) == chordToPlay.get._1) chordToPlay.set((Chord.NONE, send))
    }
  }


  private def chordOn(keysCombo: Int, send: Message => Unit) {
    if (chordMap.containsKey(keysCombo)) {
      val chord = chordMap.get(keysCombo)
      chordToPlay.set((chord, send))
      timer = 0L
    }
  }

  private def getKeysCombo(key: Int, state: Int, keysCombo: Int): Int = {
    if (state == PRESSED)
      keysCombo | (1 << key)
    else
      keysCombo & ~(1 << key)
  }

  private def handler() {
    val c = chordToPlay.get
    if (c != null) {
      if (timer > 10) {
        val chordBytes = c._1.chord.getBytes
        val send = c._2
        send(new Message(new MetaMessage(2, chordBytes, chordBytes.length), 0, c._1, timeStamp = 0L))
        chordToPlay.set(null)
        timer = 0
      }
      else timer+=1
    }
  }

}
