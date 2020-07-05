package com.codetinkerhack.example

import java.util._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors}

import com.codetinkerhack.midi.{Chord, MidiNode}
import javax.sound.midi._


object Extensions {

  implicit class IntToBase(val digits: String) {
    def base(b: Int) = Integer.parseInt(digits, b)

    def b = base(2)

    def o = base(8)

    def x = base(16)
  }

}


class ChordReader extends MidiNode {


  import Extensions._

  private var chordToPlay = new AtomicInteger(0)
  private var chordMap = new HashMap[Integer, Chord]()
  private val RELEASED = 0
  private val PRESSED = 1
  private var keysCombo: Int = _
  private var audibleKeysCombo: Int = _
  private var executor: ExecutorService = Executors.newFixedThreadPool(1)


  // Stradella

  //F
  addToChordMap("1".b, new Chord("F maj"))
  // Fm
  addToChordMap("101".b, new Chord("F min"))
  // F7
  addToChordMap("10001".b, new Chord("F 7"))
  // Fm7
  addToChordMap("10101".b, new Chord("F m7"))
  // F#maj7
  addToChordMap("1101".b, new Chord("F maj7"))

  // C
  addToChordMap("10".b, new Chord("C maj"))
  // Cm
  addToChordMap("1010".b, new Chord("C min"))
  // C7
  addToChordMap("100010".b, new Chord("C 7"))
  // Cm7
  addToChordMap("101010".b, new Chord("C m7"))


  // G
  addToChordMap("100".b, new Chord("G maj"))
  // Gm
  addToChordMap("10100".b, new Chord("G min"))
  // G7
  addToChordMap("1000100".b, new Chord("G 7"))
  // Gm7
  addToChordMap("1010100".b, new Chord("G m7"))


  // D
  addToChordMap("1000".b, new Chord("D maj"))
  // Dm
  addToChordMap("101000".b, new Chord("D min"))
  // D7
  addToChordMap("10001000".b, new Chord("D 7"))
  // Dm7
  addToChordMap("10101000".b, new Chord("D m7"))


  // A
  addToChordMap("10000".b, new Chord("A maj"))
  // Am
  addToChordMap("1010000".b, new Chord("A min"))
  // A7
  addToChordMap("100010000".b, new Chord("A 7"))
  // Am7
  addToChordMap("101010000".b, new Chord("A m7"))


  // E
  addToChordMap("100000".b, new Chord("E maj"))
  // E
  addToChordMap("10100000".b, new Chord("E min"))
  // E7
  addToChordMap("1000100000".b, new Chord("E 7"))
  // Em7
  addToChordMap("1010100000".b, new Chord("E m7"))

  // B
  addToChordMap("1000000".b, new Chord("B maj"))
  // Bm
  addToChordMap("101000000".b, new Chord("B min"))
  // B7
  addToChordMap("10001000000".b, new Chord("B 7"))
  // Bm7
  addToChordMap("10101000000".b, new Chord("B m7"))

  // F#
  addToChordMap("10000000".b, new Chord("F# maj"))
  // F#m
  addToChordMap("1010000000".b, new Chord("F# min"))
  // F#7
  addToChordMap("100010000000".b, new Chord("F# 7"))
  // F#m7
  addToChordMap("101010000000".b, new Chord("F# m7"))

  // C#
  addToChordMap("100000000".b, new Chord("C# maj"))
  // C#m
  addToChordMap("10100000000".b, new Chord("C# min"))
  // C#7
  addToChordMap("1000100000000".b, new Chord("C# 7"))
  // C#m7
  addToChordMap("1010100000000".b, new Chord("C# m7"))

  // G#
  addToChordMap("1000000000".b, new Chord("G# maj"))
  // G#m
  addToChordMap("101000000000".b, new Chord("G# min"))
  // G#7
  addToChordMap("10001000000000".b, new Chord("G# 7"))
  // G#m7
  addToChordMap("10101000000000".b, new Chord("G# m7"))

  // D#
  addToChordMap("10000000000".b, new Chord("D# maj"))
  // D#m
  addToChordMap("1010000000000".b, new Chord("D# min"))
  // D#7
  addToChordMap("100010000000000".b, new Chord("D# 7"))
  // D#m
  addToChordMap("101010000000000".b, new Chord("D# m7"))

  // A#
  addToChordMap("100000000000".b, new Chord("A# maj"))
  // A#m
  addToChordMap("10100000000000".b, new Chord("A# min"))
  // A#7
  addToChordMap("1000100000000000".b, new Chord("A# 7"))
  // A#m
  addToChordMap("1010100000000000".b, new Chord("A# m7"))

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

  private def addToChordMap(keysCombo: Int, c: Chord) {
    chordMap.put(keysCombo, c)
  }

  private def chordOff(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
      if (keysCombo == chordToPlay.get) chordToPlay.set(0)
    }
  }


  private def chordOn(keysCombo: Int) {
    if (chordMap.containsKey(keysCombo)) {
      chordToPlay.set(keysCombo)
      executor.submit(new ChordsPlayer())
    }
  }

  private def getKeysCombo(key: Int, state: Int, keysCombo: Int): Int = {
    val k = key

    if (state == 1) keysCombo | (1 << k) else keysCombo & ~(1 << k)

  }

  private class ChordsPlayer extends Runnable {

    override def run() {
      try {
        Thread.sleep(50) // We need to delay playing chord to allow sufficient time to press all the chord keysss
      } catch {
        case e: InterruptedException => e.printStackTrace()
      }
      val keysCombo = chordToPlay.getAndSet(0)

      val chord = chordMap.get(keysCombo)
      val chordBytes = chord.chord.getBytes

      send(Some(new MetaMessage(2, chordBytes, chordBytes.length)), 0)

      // println(chord.chord)
      //       chordMap.get(keysCombo).getChordNotes() foreach {
      //        note =>
      //        Try {
      //          println(note)
      //          //receiver.send(new ShortMessage(ShortMessage.NOTE_ON, note + 12, 64), 0)
      //        }
      //      }
    }
  }

}
