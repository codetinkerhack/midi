package com.codetinkerhack.midi

import java.util._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, Future}

import javax.sound.midi._

import scala.collection.immutable.List


class ChordReader extends MidiNode {


  private val chordToPlay = new AtomicInteger(0)
  private val chordMap = new HashMap[Integer, Chord]()
  private val RELEASED = 0
  private val PRESSED = 1
  private var keysCombo: Int = 0
  private var audibleKeysCombo: Int = 0
  private val executor: ExecutorService = Executors.newFixedThreadPool(1)
  private var chordScheduled: Future[_] = _

  override def getName(): String = "ChordReader"

  override def processMessage(message: MidiMessageContainer, send: MidiMessageContainer => Unit): Unit = {

    message.get match {
      case m: ShortMessage if (m.getCommand == ShortMessage.NOTE_ON) =>
        val keysCombo = getKeysCombo(m.getData1, PRESSED, this.keysCombo)
        if (chordMap.containsKey(keysCombo)) {
          chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo
          chordOn(keysCombo, send)
          val nothing = new String()
          this.keysCombo = keysCombo
          send(new MidiMessageContainer(new MetaMessage(1, nothing.getBytes(), nothing.getBytes().length), message.getDepth, Chord.NONE, timeStamp = 0L))
        }

      case m: ShortMessage if (m.getCommand == ShortMessage.NOTE_OFF) =>
        val keysCombo = getKeysCombo(m.getData1, RELEASED, this.keysCombo)
        if (chordMap.containsKey(keysCombo) || keysCombo == 0) {
          if (audibleKeysCombo != 0) chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo
          chordOn(keysCombo, send)
          this.keysCombo = keysCombo
          val nothing = new String()
          send(new MidiMessageContainer(new MetaMessage(1, nothing.getBytes(), nothing.getBytes().length), message.getDepth,  Chord.NONE, timeStamp = 0L))
        }

      case _ => send(message)

    }

  }

  def addToChordMap(keysCombo: Int, c: Chord) {
    chordMap.put(keysCombo, c)
  }

  private def chordOff(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
      if (keysCombo == chordToPlay.get) chordToPlay.set(0)
    }
  }


  private def chordOn(keysCombo: Int, send: MidiMessageContainer => Unit) {
    if (chordMap.containsKey(keysCombo)) {
//      if(chordScheduled  != null)
//        chordScheduled.cancel(true)

      chordToPlay.set(keysCombo)
      chordScheduled = executor.submit(new ChordsPlayer(send))

    }
  }

  private def getKeysCombo(key: Int, state: Int, keysCombo: Int): Int = {
    if (state == PRESSED)
      keysCombo | (1 << key)
    else
      keysCombo & ~(1 << key)
  }

  private class ChordsPlayer(send: MidiMessageContainer => Unit) extends Runnable {

    override def run() {
      try {
        Thread.sleep(10) // We need to delay playing chord to allow sufficient time to press all the chord keys
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
      val keysCombo = chordToPlay.getAndSet(0)

      val chord = chordMap.get(keysCombo)
      val chordBytes = chord.chord.getBytes

      send(new MidiMessageContainer(new MetaMessage(2, chordBytes, chordBytes.length), 0, chord, timeStamp = 0L))
    }
  }

}
