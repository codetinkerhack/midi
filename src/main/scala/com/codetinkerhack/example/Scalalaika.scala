package com.codetinkerhack.example

import java.lang.System.currentTimeMillis

import com.codetinkerhack.midi._
import javax.sound.midi.{MetaMessage, MidiMessage, ShortMessage}

import scala.collection.immutable.List

/**
  * Created by Evgeniy on 31/01/2015.
  */
object Scalalaika extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopback")
    val inputNanoPad = mh.getTransmitters.get("PAD")

    output.open()
    inputNanoPad.open()

    val chordReader = new ChordReader()
    initChordReader(chordReader)

    val midiDelay = new MidiDelay()
    val scalaLika = getScalalaika()
    val chordModifier = new ChordModifier()
    val midiOut = MidiNode(output.getReceiver)
    val midiNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))

    chordModifier.setBaseChord(new Chord("C 7"))

    val instrumentSelector = Scalalaika.getInstrumentSelector()

    midiNanoPad.out(0).connect(instrumentSelector)

    instrumentSelector.out(0)
      .connect(chordReader).out(0)
      .connect(scalaLika)

    scalaLika.out(1).connect(midiDelay).out(1).connect(chordModifier).out(1).connect(Transposer(0)).connect(midiOut)
    scalaLika.out(2).connect(chordModifier).out(2).connect(Transposer(0)).connect(midiOut)
  }

  def getInstrumentSelector() = MidiNode (
    (message, timeStamp) => {

      val baseInstrument = IndexedSeq(0, 1, 2, 3)
      val soloInstrument = IndexedSeq(0, 1, 2, 3)

      import javax.sound.midi.ShortMessage._

      message match {
        case m: ShortMessage if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
          var messageList: List[(ShortMessage, Long)] = List()

          messageList = (new ShortMessage(PROGRAM_CHANGE, 1, baseInstrument((m.getData1 - 36) / 16), 0), 0L) :: messageList
          messageList = (new ShortMessage(PROGRAM_CHANGE, 2, soloInstrument((m.getData1 - 36) / 16), 0), 0L) :: messageList
          messageList = (new ShortMessage(m.getCommand, m.getChannel, (m.getData1 - 36) % 16, 0), 0L) :: messageList

          messageList
        }
        case _ => List((message, timeStamp))
      }
    })

  def getScalalaika() = new MidiNode {


    private val baseNote = 48
    private val scale = Array(0, 4, 7, 10, 16)

    private var currentChord: Chord = _

    private var timeLapsed = 0L
    private var notesOnCache = Set[Int]()
    private var offset = 0


    override def processMessage(message: MidiMessage, timeStamp: Long): List[(MidiMessage, Long)] = {
      import ShortMessage._

      message match {

        case m: MetaMessage => {
          val newChord = new Chord(new String(m.getData))
          println(s"Chord received: ${newChord}")

          if (currentChord != newChord) {

            currentChord = newChord

            val notesOff = notesOnCache.map(n => (new ShortMessage(NOTE_OFF, 2, n, 0), 0l))

            notesOnCache = Set.empty

            notesOff.toList  ::: ((message, 0L) :: List.empty)
          } else
            List((message, timeStamp))
        }

        case message: ShortMessage if (message.getCommand == CONTROL_CHANGE && message.getData1 == 2 && currentChord != null) => {
          val ccy = message.getData2

          val note = baseNote + scale((128 - ccy) / 26) + offset

          var noteList = List.empty[(ShortMessage, Long)]

          if (!notesOnCache(note) || (notesOnCache(note) && (currentTimeMillis() - timeLapsed) > 50)) {

            var notesOff = Set[(ShortMessage, Long)]()
            if (currentTimeMillis() - timeLapsed > 100) {
              notesOff = notesOnCache.map(n => (new ShortMessage(NOTE_OFF, 2, n, 0), 0L))

              notesOnCache = Set.empty
              noteList = noteList ::: notesOff.toList
            }

            timeLapsed = currentTimeMillis()

            notesOnCache = notesOnCache + note

            noteList = noteList ::: List((new ShortMessage(NOTE_ON, 2, note, 64), 20L))
          }

          noteList
        }
        case message: ShortMessage if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
          //println("Control change x: " + x.getData2);
          offset = (message.getData2 / 32)
//          List((new ShortMessage(PITCH_BEND, 2, 0, message.getData2 / 26 - message.getData2 % 26), 0l))
          List()
        }

        case _ => List((message, timeStamp))
      }

    }
  }

  def initChordReader(chordReader: ChordReader) = {

    implicit class IntToBase(val digits: String) {
      def b: Int = Integer.parseInt(digits, 2)
    }

    // Key combos to Chord mapping

    //F
    chordReader.addToChordMap("1".b, new Chord("F maj"))
    // Fm
    chordReader.addToChordMap("101".b, new Chord("F min"))
    // F7
    chordReader.addToChordMap("10001".b, new Chord("F 7"))
    // Fm7
    chordReader.addToChordMap("10101".b, new Chord("F m7"))
    // Fmaj7
    chordReader.addToChordMap("1101".b, new Chord("F maj7"))

    // C
    chordReader.addToChordMap("10".b, new Chord("C maj"))
    // Cm
    chordReader.addToChordMap("1010".b, new Chord("C min"))
    // C7
    chordReader.addToChordMap("100010".b, new Chord("C 7"))
    // Cm7
    chordReader.addToChordMap("101010".b, new Chord("C m7"))


    // G
    chordReader.addToChordMap("100".b, new Chord("G maj"))
    // Gm
    chordReader.addToChordMap("10100".b, new Chord("G min"))
    // G7
    chordReader.addToChordMap("1000100".b, new Chord("G 7"))
    // Gm7
    chordReader.addToChordMap("1010100".b, new Chord("G m7"))


    // D
    chordReader.addToChordMap("1000".b, new Chord("D maj"))
    // Dm
    chordReader.addToChordMap("101000".b, new Chord("D min"))
    // D7
    chordReader.addToChordMap("10001000".b, new Chord("D 7"))
    // Dm7
    chordReader.addToChordMap("10101000".b, new Chord("D m7"))


    // A
    chordReader.addToChordMap("10000".b, new Chord("A maj"))
    // Am
    chordReader.addToChordMap("1010000".b, new Chord("A min"))
    // A7
    chordReader.addToChordMap("100010000".b, new Chord("A 7"))
    // Am7
    chordReader.addToChordMap("101010000".b, new Chord("A m7"))


    // E
    chordReader.addToChordMap("100000".b, new Chord("E maj"))
    // E
    chordReader.addToChordMap("10100000".b, new Chord("E min"))
    // E7
    chordReader.addToChordMap("1000100000".b, new Chord("E 7"))
    // Em7
    chordReader.addToChordMap("1010100000".b, new Chord("E m7"))

    // B
    chordReader.addToChordMap("1000000".b, new Chord("B maj"))
    // Bm
    chordReader.addToChordMap("101000000".b, new Chord("B min"))
    // B7
    chordReader.addToChordMap("10001000000".b, new Chord("B 7"))
    // Bm7
    chordReader.addToChordMap("10101000000".b, new Chord("B m7"))

    // F#
    chordReader.addToChordMap("10000000".b, new Chord("F# maj"))
    // F#m
    chordReader.addToChordMap("1010000000".b, new Chord("F# min"))
    // F#7
    chordReader.addToChordMap("100010000000".b, new Chord("F# 7"))
    // F#m7
    chordReader.addToChordMap("101010000000".b, new Chord("F# m7"))

    // C#
    chordReader.addToChordMap("100000000".b, new Chord("C# maj"))
    // C#m
    chordReader.addToChordMap("10100000000".b, new Chord("C# min"))
    // C#7
    chordReader.addToChordMap("1000100000000".b, new Chord("C# 7"))
    // C#m7
    chordReader.addToChordMap("1010100000000".b, new Chord("C# m7"))

    // G#
    chordReader.addToChordMap("1000000000".b, new Chord("G# maj"))
    // G#m
    chordReader.addToChordMap("101000000000".b, new Chord("G# min"))
    // G#7
    chordReader.addToChordMap("10001000000000".b, new Chord("G# 7"))
    // G#m7
    chordReader.addToChordMap("10101000000000".b, new Chord("G# m7"))

    // D#
    chordReader.addToChordMap("10000000000".b, new Chord("D# maj"))
    // D#m
    chordReader.addToChordMap("1010000000000".b, new Chord("D# min"))
    // D#7
    chordReader.addToChordMap("100010000000000".b, new Chord("D# 7"))
    // D#m
    chordReader.addToChordMap("101010000000000".b, new Chord("D# m7"))

    // A#
    chordReader.addToChordMap("100000000000".b, new Chord("A# maj"))
    // A#m
    chordReader.addToChordMap("10100000000000".b, new Chord("A# min"))
    // A#7
    chordReader.addToChordMap("1000100000000000".b, new Chord("A# 7"))
    // A#m
    chordReader.addToChordMap("1010100000000000".b, new Chord("A# m7"))
  }
}