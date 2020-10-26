package com.codetinkerhack.example

import java.lang.System.currentTimeMillis

import com.codetinkerhack.midi._
import javax.sound.midi.ShortMessage.{CONTROL_CHANGE, NOTE_OFF, NOTE_ON}
import javax.sound.midi.{MetaMessage, ShortMessage}

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
    val chordKeysReader = getChordKeysReader()
    val chordModifier = new ChordModifier()
    val midiOut = MidiNode(output.getReceiver)
    val midiNanoPad = MidiNode()


    val chain = midiNanoPad
      .connect(MidiFilter { message =>
      import ShortMessage._
      message.get match {
        case m: ShortMessage if m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF => true
//        case m: ShortMessage if m.getCommand == CONTROL_CHANGE => false
        case _ => false
      }
    })
      .connect(chordKeysReader)
      .connect(chordReader)
      .connect(scalaLika.in(0))
      .connect(chordModifier)
      .connect(MidiFilter { message =>
        import ShortMessage._
        message.get match {
          case m: ShortMessage if m.getCommand == PITCH_BEND => true
          case m: ShortMessage if m.getCommand == PROGRAM_CHANGE => true
          case m: ShortMessage if m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF => true
          case _ => false
        }
      })
      .connect(MidiUtil.debugMidi)
      .connect(midiOut)

    val chain1 = midiNanoPad
      .connect(ChannelRouter(1)).connect(MidiFilter { message =>
        import ShortMessage._
        message.get match {
          case m: ShortMessage if m.getCommand == CONTROL_CHANGE => true
          case _ => false
        }
      })
      .connect(scalaLika.in(1))
      .connect(midiDelay)
      .connect(chordModifier)
      .connect(MidiFilter { message =>
        import ShortMessage._
        message.get match {
          case m: ShortMessage if m.getCommand == PITCH_BEND => true
          case m: ShortMessage if m.getCommand == PROGRAM_CHANGE => true
          case m: ShortMessage if m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF => true
          case _ => false
        }
      })
      .connect(MidiUtil.debugMidi)
      .connect(midiOut)

    val chain2 = MidiParallel(chain, chain1)
    MidiNode(inputNanoPad.getTransmitters.get(0)).connect(chain2)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_ON, 0, 36, 0)), 0L, null)
//    Thread.sleep(100)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(CONTROL_CHANGE, 0, 2, 0)), 0L, null)
//    Thread.sleep(1000)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_OFF, 0, 36, 0)), 0L, null)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_ON, 0, 37, 0)), 0L, null)
//    Thread.sleep(100)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(CONTROL_CHANGE, 0, 2, 30)), 0L, null)
  }



  def getChordKeysReader() = MidiNode ( "ChordKeysReader",

    (message, timeStamp) => {

      val soloInstrument = IndexedSeq(1, 2, 3, 4)

      import javax.sound.midi.ShortMessage._

      message.get match {
        case m: ShortMessage if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
          var messageList: List[(MidiMessageContainer, Long)] = List()
          //messageList = (new ShortMessage(PROGRAM_CHANGE, 1, soloInstrument((m.getData1 - 36) / 16), 0), 0L) :: messageList
          messageList = (new MidiMessageContainer(new ShortMessage(m.getCommand, 0, (m.getData1 - 36) % 16, m.getData2)), 0L) :: messageList
          messageList
        }
        case _ => List((message, timeStamp))
      }
    })

  def getScalalaika() = new MidiNode {

    override def getName(): String = "Skalalika"

    private val baseNote = 48
    private val scale = Array(0, 4, 7, 10, 16)

    private var currentChord: Chord = _

    private var timeLapsed = 0L
    private var notesOnCache = Set[Int]()
    private var offset = 0


    override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit = {
      import ShortMessage._

      message.get match {

        case m: MetaMessage if m.getType == 2 => {
          val newChord = new Chord(new String(m.getData))
          if (currentChord == null || (currentChord.chord != newChord.chord && newChord.chord != Chord.NONE)) {
            currentChord = newChord
            notesOnCache.foreach(n => send(new MidiMessageContainer(new ShortMessage(NOTE_OFF, 1, n, 0)), 0L, chain))
            notesOnCache = Set.empty
          }
          send(message, timeStamp, chain)
        }

        case m: ShortMessage if (m.getCommand == CONTROL_CHANGE && m.getData1 == 2 && currentChord != null) => {
          val ccy = m.getData2

          val note = baseNote + scale((128 - ccy) / 26) + offset

          if (!notesOnCache(note) || (notesOnCache(note) && (currentTimeMillis() - timeLapsed) > 50)) {
            if (currentTimeMillis() - timeLapsed > 100) {
              notesOnCache.foreach(n => send(new MidiMessageContainer(new ShortMessage(NOTE_OFF, 1, n, 0)), 0L, chain))
              notesOnCache = Set.empty
            }
            timeLapsed = currentTimeMillis()
            notesOnCache = notesOnCache + note
            send(new MidiMessageContainer(new ShortMessage(NOTE_ON, 1, note, 64)), 0L, chain)
          }
        }

        case message: ShortMessage if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
          offset = (message.getData2 / 32)
          send(new MidiMessageContainer(new ShortMessage(PITCH_BEND, 1, 0, message.getData2 % 8)), 0l, chain)
        }

        case _ => send(message, timeStamp, chain)
      }

    }
  }

  def initChordReader(chordReader: ChordReader) = {

    implicit class IntToBase(val digits: String) {
      def b: Int = Integer.parseInt(digits, 2)
    }

    // Key combos to Chord mapping

    //No chord - all off
    chordReader.addToChordMap("0".b, new Chord("N N"))
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