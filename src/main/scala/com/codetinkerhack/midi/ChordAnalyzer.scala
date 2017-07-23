package com.codetinkerhack.midi

import java.util._
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors}
import javax.sound.midi._


object Extensions {

  implicit class IntToBase(val digits: String) {
    def base(b: Int) = Integer.parseInt(digits, b)

    def b = base(2)

    def o = base(8)

    def x = base(16)
  }

}


class ChordAnalyzer extends MidiNode {

  import Extensions._

  private var chordToPlay = new AtomicInteger(0)
  private var chordMap = new HashMap[Integer, Chord]()
  private val RELEASED = 0
  private val PRESSED = 1
  private var keysCombo: Int = _
  private var audibleKeysCombo: Int = _
  private var executor: ExecutorService = Executors.newFixedThreadPool(1)

  //  @BeanProperty
  //  var chordTransformer: ChordTransformer = _


  /*
  // G
  addToChordMap(0b1, Note_G, Note_B, Note_D1);

  // F
  addToChordMap(0b10, Note_F, Note_A, Note_C1);

  // Fm
    addToChordMap(0b11, Note_F, Note_Gx, Note_C1);


  // C
  addToChordMap(0b100, Note_C1, Note_E, Note_G);

  // Cm
     addToChordMap(0b101, Note_C1, Note_Dx, Note_G);

  // C7
    addToChordMap(0b111, Note_C, Note_E, Note_G, Note_Ax);


  // Am
  addToChordMap(0b1000, Note_A, Note_C1, Note_E);

  // Dm
  addToChordMap(0b10000, Note_D1, Note_F, Note_A);

  // Em
  addToChordMap(0b100000, Note_E, Note_G, Note_B);

  // E
    addToChordMap(0b101000, Note_E, Note_Gx, Note_B);


  // D
  addToChordMap(0b10100, Note_D1, Note_Fx, Note_A);


  // Bb
  addToChordMap(0b1000000, Note_Ax, Note_D, Note_F);

  // A
  addToChordMap(0b1010, Note_A, Note_Cx, Note_E);



  // B7
  addToChordMap(0b10000000, Note_B, Note_Dx, Note_Fx, Note_A);


  // D7
  addToChordMap(0b100000000, Note_D1, Note_Fx, Note_A, Note_C);


  // E7
  addToChordMap(0b1000000000, Note_E, Note_Gx, Note_B, Note_D1);



  // G7
  addToChordMap(0b10000000000, Note_G, Note_B, Note_D1, Note_F);

  // A7
  addToChordMap(0b100000000000, Note_A, Note_C, Note_E, Note_G);
*/
  // Stradella

  //F
  addToChordMap("1".b, new Chord("F maj"))

  // Fm
  addToChordMap("101".b, new Chord("F min"))

  // C
  addToChordMap("10".b, new Chord("C maj"))

  // Cm
  addToChordMap("1010".b, new Chord("C min"))

  // C7
  addToChordMap("1110".b, new Chord("C 7"))

  // G
  addToChordMap("100".b, new Chord("G maj"))

  // G7
  addToChordMap("11100".b, new Chord("G 7"))

  // D
  addToChordMap("1000".b, new Chord("D maj"))

  // D7
  addToChordMap("111000".b, new Chord("D 7"))

  // A
  addToChordMap("10000".b, new Chord("A maj"))

  // A7
  addToChordMap("1110000".b, new Chord("A 7"))

  // E
  addToChordMap("100000".b, new Chord("E maj"))

  // E7
  addToChordMap("11100000".b, new Chord("E 7"))

  // B
  addToChordMap("1000000".b, new Chord("B maj"))

  // Bb
  addToChordMap("101000000".b, new Chord("A# min"))

  // B7
  addToChordMap("111000000".b, new Chord("B 7"))

  // F#
  addToChordMap("10000000".b, new Chord("F# maj"))

  // G#
  addToChordMap("100000000".b, new Chord("G# maj"))

  // D#
  addToChordMap("1000000000".b, new Chord("D# maj"))

  // A#
  addToChordMap("10000000000".b, new Chord("A# maj"))


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
