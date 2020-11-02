package com.codetinkerhack.example
import java.lang.System.currentTimeMillis
import com.codetinkerhack.midi._
import javax.sound.midi.{MetaMessage, ShortMessage}
import ShortMessage._
import scala.collection.immutable.List

/**
  * Created by Evgeniy on 31/01/2015.
  */
object Strumzer extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopback")
    val inputNanoPad = mh.getTransmitters.get("PAD")

    output.open()
    inputNanoPad.open()

    val chordReader = new ChordReader()
    initChordReader(chordReader)

    val midiNanoPad = MidiNode()
    val midiDelay = new MidiDelay()
    val strumzer = getStrumzer()
    val chordKeysReader = getChordKeysReader()
    val chordModifier = new ChordModifier()
    val midiOut = MidiNode(output.getReceiver)

    val chain = midiNanoPad
      .connect(MidiFilter { message =>
        message.get match {
          case m: ShortMessage if m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF => true
          case _ => false
        }
      })
      .connect(chordKeysReader)
      .connect(chordReader)
      .connect(strumzer.in(0))
      .connect(chordModifier)
      .connect(MidiFilter { message =>
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
        message.get match {
          case m: ShortMessage if m.getCommand == CONTROL_CHANGE => true
          case _ => false
        }
      })
      .connect(strumzer.in(1))
      .connect(midiDelay)
      .connect(chordModifier)
      .connect(MidiFilter { message =>
        message.get match {
          case m: ShortMessage if m.getCommand == PITCH_BEND => true
          case m: ShortMessage if m.getCommand == PROGRAM_CHANGE => true
          case m: ShortMessage if m.getCommand == NOTE_ON || m.getCommand == NOTE_OFF => true
          case _ => false
        }
      })
      .connect(MidiUtil.debugMidi)
      .connect(midiOut)

    val chain2 = MidiParallel(chain1, chain)
    MidiNode(inputNanoPad.getTransmitters.get(0)).connect(chain2)
    import javax.sound.midi.ShortMessage._
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_ON, 0, 36, 0)), 0L, null)
//    Thread.sleep(100)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(CONTROL_CHANGE, 0, 2, 0)), 0L, null)
//    Thread.sleep(1000)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_OFF, 0, 36, 0)), 0L, null)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(NOTE_ON, 0, 37, 0)), 0L, null)
//    Thread.sleep(100)
//    chain2.processMessage(new MidiMessageContainer(new ShortMessage(CONTROL_CHANGE, 0, 2, 30)), 0L, null)
  }



  def getChordKeysReader() = MidiNode ("ChordKeysReader",

    message => {

      val soloInstrument = IndexedSeq(1, 2, 3, 4)

      import javax.sound.midi.ShortMessage._

      message.get match {
        case m: ShortMessage if m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON => {
          var messageList: List[Message] = List()
          //messageList = (new ShortMessage(PROGRAM_CHANGE, 1, soloInstrument((m.getData1 - 36) / 16), 0), 0L) :: messageList
          messageList = new Message(new ShortMessage(m.getCommand, 0, (m.getData1 - 36) % 16, m.getData2)) :: messageList
          messageList
        }
        case _ => List(message)
      }
    })

  def getStrumzer() = new MidiNode {

    override def getName() = "Strumzer"

    private val baseNote = 48
    private val scale = Array(0, 4, 7, 10, 16)

    private var currentChord = Chord.NONE

    private var timeLapsed = 0L
    private var notesOnCache = Set[Message]()
    private var offset = 0


    override def processMessage(message: Message, send: Message => Unit): Unit = {
      message.get match {

        case m: MetaMessage if m.getType == 2 => {
          val newChord = message.getChord
          if (currentChord == null || (currentChord.chord != newChord.chord && newChord != Chord.NONE)) {
            currentChord = newChord
            notesOnCache.foreach(n => send(n.getNoteOff()))
            notesOnCache = Set.empty
          }
          send(message)
        }

        case m: ShortMessage if m.getCommand == CONTROL_CHANGE && m.getData1 == 2 && currentChord != Chord.NONE => {
          val ccy = m.getData2

          val note = baseNote + scale((128 - ccy) / 26) + offset
          val midiCNote = new Message(new ShortMessage(ShortMessage.NOTE_ON, 1, note, 64), chord = currentChord)

          if (!notesOnCache(midiCNote) || (notesOnCache(midiCNote) && (currentTimeMillis() - timeLapsed) > 50)) {
            if (currentTimeMillis() - timeLapsed > 100) {
              notesOnCache.foreach(n => send(n.getNoteOff()))
              notesOnCache = Set.empty
            }
            timeLapsed = currentTimeMillis()
            notesOnCache = notesOnCache + midiCNote
            send(midiCNote)
          }
        }

        case message: ShortMessage if message.getCommand == CONTROL_CHANGE && message.getData1 == 1 => {
          offset = message.getData2 / 32
          send(new Message(new ShortMessage(PITCH_BEND, 1, 0, message.getData2 % 8)))
        }

        case _ => send(message)
      }
    }
  }

  def initChordReader(chordReader: ChordReader) = {

    implicit class IntToBase(val digits: String) {
      def b: Int = Integer.parseInt(digits, 2)
    }

    // Key combos to Chord mapping

    //No chord - all off
    chordReader.addToChordMap("0".b, Chord.NONE)

    chordReader.addToChordMap("1".b, new Chord("F maj"))
    chordReader.addToChordMap("101".b, new Chord("F min"))
    chordReader.addToChordMap("10001".b, new Chord("F 7"))
    chordReader.addToChordMap("10101".b, new Chord("F m7"))
    chordReader.addToChordMap("1101".b, new Chord("F maj7"))

    chordReader.addToChordMap("10".b, new Chord("C maj"))
    chordReader.addToChordMap("1010".b, new Chord("C min"))
    chordReader.addToChordMap("100010".b, new Chord("C 7"))
    chordReader.addToChordMap("101010".b, new Chord("C m7"))

    chordReader.addToChordMap("100".b, new Chord("G maj"))
    chordReader.addToChordMap("10100".b, new Chord("G min"))
    chordReader.addToChordMap("1000100".b, new Chord("G 7"))
    chordReader.addToChordMap("1010100".b, new Chord("G m7"))

    chordReader.addToChordMap("1000".b, new Chord("D maj"))
    chordReader.addToChordMap("101000".b, new Chord("D min"))
    chordReader.addToChordMap("10001000".b, new Chord("D 7"))
    chordReader.addToChordMap("10101000".b, new Chord("D m7"))

    chordReader.addToChordMap("10000".b, new Chord("A maj"))
    chordReader.addToChordMap("1010000".b, new Chord("A min"))
    chordReader.addToChordMap("100010000".b, new Chord("A 7"))
    chordReader.addToChordMap("101010000".b, new Chord("A m7"))

    chordReader.addToChordMap("100000".b, new Chord("E maj"))
    chordReader.addToChordMap("10100000".b, new Chord("E min"))
    chordReader.addToChordMap("1000100000".b, new Chord("E 7"))
    chordReader.addToChordMap("1010100000".b, new Chord("E m7"))

    chordReader.addToChordMap("1000000".b, new Chord("B maj"))
    chordReader.addToChordMap("101000000".b, new Chord("B min"))
    chordReader.addToChordMap("10001000000".b, new Chord("B 7"))
    chordReader.addToChordMap("10101000000".b, new Chord("B m7"))

    chordReader.addToChordMap("10000000".b, new Chord("F# maj"))
    chordReader.addToChordMap("1010000000".b, new Chord("F# min"))
    chordReader.addToChordMap("100010000000".b, new Chord("F# 7"))
    chordReader.addToChordMap("101010000000".b, new Chord("F# m7"))

    chordReader.addToChordMap("100000000".b, new Chord("C# maj"))
    chordReader.addToChordMap("10100000000".b, new Chord("C# min"))
    chordReader.addToChordMap("1000100000000".b, new Chord("C# 7"))
    chordReader.addToChordMap("1010100000000".b, new Chord("C# m7"))

    chordReader.addToChordMap("1000000000".b, new Chord("G# maj"))
    chordReader.addToChordMap("101000000000".b, new Chord("G# min"))
    chordReader.addToChordMap("10001000000000".b, new Chord("G# 7"))
    chordReader.addToChordMap("10101000000000".b, new Chord("G# m7"))

    chordReader.addToChordMap("10000000000".b, new Chord("D# maj"))
    chordReader.addToChordMap("1010000000000".b, new Chord("D# min"))
    chordReader.addToChordMap("100010000000000".b, new Chord("D# 7"))
    chordReader.addToChordMap("101010000000000".b, new Chord("D# m7"))

    chordReader.addToChordMap("100000000000".b, new Chord("A# maj"))
    chordReader.addToChordMap("10100000000000".b, new Chord("A# min"))
    chordReader.addToChordMap("1000100000000000".b, new Chord("A# 7"))
    chordReader.addToChordMap("1010100000000000".b, new Chord("A# m7"))
  }
}