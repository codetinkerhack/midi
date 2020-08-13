package com.codetinkerhack.midi

import java.util._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, Future}

import javax.sound.midi._


class ChordReader extends MidiNode {


  private val chordToPlay = new AtomicInteger(0)
  private val chordMap = new HashMap[Integer, Chord]()
  private val RELEASED = 0
  private val PRESSED = 1
  private var keysCombo: Int = _
  private var audibleKeysCombo: Int = _
  private val executor: ExecutorService = Executors.newFixedThreadPool(1)
  private var chordScheduled: Future[_] = _

  override def receive(message: Option[MidiMessage], timeStamp: Long) {

    message match {
      case Some(m: ShortMessage) if (m.getCommand == ShortMessage.NOTE_ON) =>
        //println(s"Note Chord analyzer: ${m.getData1} , channel: ${m.getChannel}")
        val keysCombo = getKeysCombo(m.getData1, PRESSED, this.keysCombo)
        if (chordMap.containsKey(keysCombo)) {
          chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo
          chordOn(keysCombo)
        }
        this.keysCombo = keysCombo

      case Some(m: ShortMessage) if (m.getCommand == ShortMessage.NOTE_OFF) =>
        val keysCombo1 = getKeysCombo(m.getData1, RELEASED, this.keysCombo)
        if (chordMap.containsKey(keysCombo1) || keysCombo1 == 0) {
          if (audibleKeysCombo != null) chordOff(audibleKeysCombo)
          this.audibleKeysCombo = keysCombo1
          chordOn(keysCombo1)
        }
        this.keysCombo = keysCombo1

      case _ =>

    }

    send(message, timeStamp)
  }

  def addToChordMap(keysCombo: Int, c: Chord) {
    chordMap.put(keysCombo, c)
  }

  private def chordOff(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
      if (keysCombo == chordToPlay.get) chordToPlay.set(0)
    }
  }


  private def chordOn(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
//      if(chordScheduled  != null)
//        chordScheduled.cancel(true)

      chordToPlay.set(keysCombo)
      chordScheduled = executor.submit(new ChordsPlayer())

    }
  }

  private def getKeysCombo(key: Int, state: Int, keysCombo: Int): Int = {
    if (state == PRESSED)
      keysCombo | (1 << key)
    else
      keysCombo & ~(1 << key)
  }

  private class ChordsPlayer extends Runnable {

    override def run() {
      try {
        Thread.sleep(10) // We need to delay playing chord to allow sufficient time to press all the chord keys
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
      val keysCombo = chordToPlay.getAndSet(0)

      val chord = chordMap.get(keysCombo)
      val chordBytes = chord.chord.getBytes

      send(Some(new MetaMessage(2, chordBytes, chordBytes.length)), 0)
    }
  }

}
