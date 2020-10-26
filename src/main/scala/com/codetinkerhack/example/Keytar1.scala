//package com.codetinkerhack.example
//
//import com.codetinkerhack.example.Scalalaika.initChordReader
//import javax.sound.midi._
//import com.codetinkerhack.midi._
//
//import scala.collection.immutable.List
//
//
///**
//  * Created by Evgeniy on 31/01/2015.
//  */
//object Keytar1 extends App {
//
//  override def main(args: Array[String]) {
//
//    val mh = new MidiHandler()
//    val output = mh.getReceivers.get("loopback")
//    val inputNanoPad = mh.getTransmitters.get("PAD")
//    val inputNanoKey = mh.getTransmitters.get("KEYBOARD")
//
//    output.open()
//    inputNanoPad.open()
//    inputNanoKey.open()
//
//    val midiDelay = new MidiDelay()
//    val keytar = new Keytar1()
//    val chordModifier = new ChordModifier()
//    chordModifier.setBaseChord(new Chord("C 7"))
//
//    val midiOut = MidiNode(output.getReceiver)
//    val midiInNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))
//
//    val chordReader = new ChordReader()
//    initChordReader(chordReader)
//
//    val midiInNanoKey = MidiNode(inputNanoKey.getTransmitters.get(0))
//
//    val instrumentSelector = MidiNode((message: MidiMessage, timeStamp: Long) => {
//
//      val baseInstrument = IndexedSeq(26, 30, 5, 7)
//      val soloInstrument = IndexedSeq(26, 29, 10, 40)
//
//      import javax.sound.midi.ShortMessage._
//
//      message match {
//        case m: ShortMessage if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
//          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
//          var messageList = List[(ShortMessage, Long)]()
//
////          messageList = (new ShortMessage(PROGRAM_CHANGE, 1, baseInstrument((m.getData1 - 36) / 16), 0), 0l) :: messageList
////          messageList = (new ShortMessage(PROGRAM_CHANGE, 2, soloInstrument((m.getData1 - 36) / 16), 0), 0l) :: messageList
//          messageList = (new ShortMessage(m.getCommand, m.getChannel, (m.getData1 - 36) % 16, 0), 0l) :: messageList
//
//          messageList
//        }
//        case _ => List((message, timeStamp))
//      }
//    })
//
//
//    val tail = MidiNode()
//      .connect(midiDelay)
//      .connect(chordModifier)
//      .connect(midiOut)
//
//    midiInNanoPad.out(0)
//      .connect(instrumentSelector).out(0)
//      .connect(chordReader).out(0)
//      .connect(keytar).connect(midiDelay)
//      .connect(chordModifier)
//      .connect(midiOut)
//
//    midiInNanoKey.out(0).connect(keytar.in(2))
//
//  }
//}
//
//class Keytar1() extends MidiNode {
//  //    Emin
//  //    4, 11, 16, 19, 23, 36
//  private val baseNote = 48
//
//  private val blackNotes = Array[Int](0, 0, 0, 0 , 12, 0 , 0 , 21, 0 , 24, 0 )
//  private val whiteNotes = Array[Int](0, 4, 7, 10, 0, 16 , 19, 0 , 23, 0 , 28)
//  private val scale = (blackNotes zip whiteNotes).map( x=> x._1 + x._2)
//
//  private var notesOnCache = Set[Int]()
//  private var currentChord = new Chord("C 7")
//
//  override def processMessage(message: MidiMessage, timeStamp: Long): List[(MidiMessage, Long)] = {
//    import ShortMessage._
//
//    message match {
//
//      case m: MetaMessage => {
//        println(s"Keytar Chord received: ${  new String(m.getData()) }")
//
//        val newChord = new Chord(new String(m.getData))
//        if (currentChord != newChord) {
//          currentChord = newChord
//          val notesOff = notesOnCache.map(n => (new ShortMessage(NOTE_OFF, 2, n, 0), 0l))
//          notesOnCache = Set.empty
//          val baseNoteOff = new ShortMessage(NOTE_OFF, 0, baseNote + currentChord.getChordBaseNote, 0)
//          val baseNoteOn = new ShortMessage(NOTE_ON, 0, baseNote + newChord.getChordBaseNote, 64)
//          notesOff.toList ::: ((baseNoteOn, 20l) :: (m, 0l) :: (baseNoteOff, 0l) :: List.empty)
//        } else
//          List((message, timeStamp))
//      }
//
//      case m: ShortMessage if m.getCommand == NOTE_ON  && m.getChannel == 2 => {
//        val note = baseNote + scale(m.getData1 - 48)
//        notesOnCache = notesOnCache + note
//        List((new ShortMessage(NOTE_ON, 2, note, 64), 20L))
//      }
//
//      case m: ShortMessage if m.getCommand == NOTE_OFF  && m.getChannel == 2 => {
//        val note = baseNote + scale(m.getData1 - 48)
//        notesOnCache = notesOnCache - note
//        List((new ShortMessage(NOTE_OFF, 2, note, 0), 20L))
//      }
//
//      case m: ShortMessage if (m.getCommand == CONTROL_CHANGE && m.getData1 == 0) => {
//        List((new ShortMessage(PITCH_BEND, 0, 0, m.getData2 % 8), 0l))
//      }
//
//      case _ => {
//        List((message, timeStamp)) }
//    }
//  }
//
//  def initChordReader(chordReader: ChordReader) = {
//
//    implicit class IntToBase(val digits: String) {
//      def b: Int = Integer.parseInt(digits, 2)
//    }
//
//    // Key combos to Chord mapping
//
//    //F
//    chordReader.addToChordMap("1".b, new Chord("F maj"))
//    // Fm
//    chordReader.addToChordMap("101".b, new Chord("F min"))
//    // F7
//    chordReader.addToChordMap("10001".b, new Chord("F 7"))
//    // Fm7
//    chordReader.addToChordMap("10101".b, new Chord("F m7"))
//    // Fmaj7
//    chordReader.addToChordMap("1101".b, new Chord("F maj7"))
//
//    // C
//    chordReader.addToChordMap("10".b, new Chord("C maj"))
//    // Cm
//    chordReader.addToChordMap("1010".b, new Chord("C min"))
//    // C7
//    chordReader.addToChordMap("100010".b, new Chord("C 7"))
//    // Cm7
//    chordReader.addToChordMap("101010".b, new Chord("C m7"))
//
//
//    // G
//    chordReader.addToChordMap("100".b, new Chord("G maj"))
//    // Gm
//    chordReader.addToChordMap("10100".b, new Chord("G min"))
//    // G7
//    chordReader.addToChordMap("1000100".b, new Chord("G 7"))
//    // Gm7
//    chordReader.addToChordMap("1010100".b, new Chord("G m7"))
//
//
//    // D
//    chordReader.addToChordMap("1000".b, new Chord("D maj"))
//    // Dm
//    chordReader.addToChordMap("101000".b, new Chord("D min"))
//    // D7
//    chordReader.addToChordMap("10001000".b, new Chord("D 7"))
//    // Dm7
//    chordReader.addToChordMap("10101000".b, new Chord("D m7"))
//
//
//    // A
//    chordReader.addToChordMap("10000".b, new Chord("A maj"))
//    // Am
//    chordReader.addToChordMap("1010000".b, new Chord("A min"))
//    // A7
//    chordReader.addToChordMap("100010000".b, new Chord("A 7"))
//    // Am7
//    chordReader.addToChordMap("101010000".b, new Chord("A m7"))
//
//
//    // E
//    chordReader.addToChordMap("100000".b, new Chord("E maj"))
//    // E
//    chordReader.addToChordMap("10100000".b, new Chord("E min"))
//    // E7
//    chordReader.addToChordMap("1000100000".b, new Chord("E 7"))
//    // Em7
//    chordReader.addToChordMap("1010100000".b, new Chord("E m7"))
//
//    // B
//    chordReader.addToChordMap("1000000".b, new Chord("B maj"))
//    // Bm
//    chordReader.addToChordMap("101000000".b, new Chord("B min"))
//    // B7
//    chordReader.addToChordMap("10001000000".b, new Chord("B 7"))
//    // Bm7
//    chordReader.addToChordMap("10101000000".b, new Chord("B m7"))
//
//    // F#
//    chordReader.addToChordMap("10000000".b, new Chord("F# maj"))
//    // F#m
//    chordReader.addToChordMap("1010000000".b, new Chord("F# min"))
//    // F#7
//    chordReader.addToChordMap("100010000000".b, new Chord("F# 7"))
//    // F#m7
//    chordReader.addToChordMap("101010000000".b, new Chord("F# m7"))
//
//    // C#
//    chordReader.addToChordMap("100000000".b, new Chord("C# maj"))
//    // C#m
//    chordReader.addToChordMap("10100000000".b, new Chord("C# min"))
//    // C#7
//    chordReader.addToChordMap("1000100000000".b, new Chord("C# 7"))
//    // C#m7
//    chordReader.addToChordMap("1010100000000".b, new Chord("C# m7"))
//
//    // G#
//    chordReader.addToChordMap("1000000000".b, new Chord("G# maj"))
//    // G#m
//    chordReader.addToChordMap("101000000000".b, new Chord("G# min"))
//    // G#7
//    chordReader.addToChordMap("10001000000000".b, new Chord("G# 7"))
//    // G#m7
//    chordReader.addToChordMap("10101000000000".b, new Chord("G# m7"))
//
//    // D#
//    chordReader.addToChordMap("10000000000".b, new Chord("D# maj"))
//    // D#m
//    chordReader.addToChordMap("1010000000000".b, new Chord("D# min"))
//    // D#7
//    chordReader.addToChordMap("100010000000000".b, new Chord("D# 7"))
//    // D#m
//    chordReader.addToChordMap("101010000000000".b, new Chord("D# m7"))
//
//    // A#
//    chordReader.addToChordMap("100000000000".b, new Chord("A# maj"))
//    // A#m
//    chordReader.addToChordMap("10100000000000".b, new Chord("A# min"))
//    // A#7
//    chordReader.addToChordMap("1000100000000000".b, new Chord("A# 7"))
//    // A#m
//    chordReader.addToChordMap("1010100000000000".b, new Chord("A# m7"))
//  }
//}