package com.codetinkerhack.example

import java.lang.System._
import javax.sound.midi._

import com.codetinkerhack.midi._

import scala.collection.immutable.List


/**
  * Created by Evgeniy on 31/01/2015.
  */
object Scalalaika extends App {


  override def main(args: Array[String]) {


    val mh = new MidiHandler()
    val output = mh.getReceivers.get("midiout")
    val inputNanoPad = mh.getTransmitters.get("PAD")


    output.open()
    inputNanoPad.open()


    //input1.open()

    val chordReader = new ChordReader()
    val noopNode = new NoopNode()
    val midiDelay = new MidiDelay()
    val scalaLika = getScalalaika()
    val chordModifier = new ChordModifier()
    val midiOut = MidiNode(output.getReceiver)
    val midiNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))

    chordModifier.setBaseChord(new Chord("E min"))


    val instrumentSelector = Scalalaika.getInstrumentSelector()

    midiNanoPad.out(0).connect(instrumentSelector)

    instrumentSelector.out(0)
      .connect(chordReader).out(0)
      .connect(scalaLika).out(0)
      .connect(chordModifier).out(0)

    instrumentSelector.out(1).connect(midiOut)
    instrumentSelector.out(2).connect(midiOut)

    scalaLika.out(1).connect(midiDelay).out(1).connect(chordModifier).out(1).connect(midiOut)
    scalaLika.out(2).connect(chordModifier).out(2).connect(midiOut)

  }

  def getInstrumentSelector() = MidiNode (
    (message, timeStamp) => {

      val baseInstrument = IndexedSeq(26, 30, 5, 7)
      val soloInstrument = IndexedSeq(24, 29, 10, 40)

      import javax.sound.midi.ShortMessage._

      message match {
        case Some(m: ShortMessage) if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
          var messageList: List[(Option[ShortMessage], Long)] = List()

          messageList = (Some(new ShortMessage(PROGRAM_CHANGE, 1, baseInstrument((m.getData1 - 36) / 16), 0)), 0l) :: messageList
          messageList = (Some(new ShortMessage(PROGRAM_CHANGE, 2, soloInstrument((m.getData1 - 36) / 16), 0)), 0l) :: messageList
          messageList = (Some(new ShortMessage(m.getCommand, m.getChannel, (m.getData1 - 36) % 16, 0)), 0l) :: messageList

          messageList
        }
        case _ => List((message, timeStamp))
      }
    })

  def getScalalaika() = new MidiNode {

    //    Emin
    //    4, 11, 16, 19, 23, 36

    private val baseNote = 40

    private val scale = Array(4, 11, 16, 19, 23, 28)

    private var currentBaseNote: Option[ShortMessage] = None

    private var timeLapsed = 0l
    private var notesOnCache = Set[Int]()


    override def processMessage(message: Option[MidiMessage], timeStamp: Long): List[(Option[MidiMessage], Long)] = {
      import ShortMessage._

      message match {

        case Some(message: ShortMessage) if (message.getCommand == NOTE_ON) => {

          val note = baseNote + scale(0) - 12
          currentBaseNote = Some(new ShortMessage(NOTE_ON, 1, note, 64))

          (currentBaseNote, 60L) :: List((Some(new ShortMessage(PITCH_BEND, 1, 0, 0)), 0L))
        }

        case Some(message: ShortMessage) if (message.getCommand == NOTE_OFF) => {
          val baseNoteOff = (currentBaseNote.map(n => new ShortMessage(NOTE_OFF, 1, n.getData1, 0)), 0L)

          val notesOff = notesOnCache.map(n => (Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0l))
          notesOnCache = Set.empty
          currentBaseNote = None

          baseNoteOff :: notesOff.toList
        }

        case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 2) => {
          val ccy = message.getData2

          currentBaseNote match {

            case Some(m1: ShortMessage) => {

//              println("Control change y: " + ccy)

              val note = baseNote + scale((128 - ccy) / 32)

              var noteList = List.empty[(Some[ShortMessage], Long)]

              if (!notesOnCache(note) || (notesOnCache(note) && (currentTimeMillis() - timeLapsed) > 50)) {

                var notesOff = Set[(Some[ShortMessage], Long)]()
                if (currentTimeMillis() - timeLapsed > 100) {
                  notesOff = notesOnCache.map(n => (Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0l))

                  notesOnCache = Set.empty
                  noteList = noteList ::: notesOff.toList
                }

                timeLapsed = currentTimeMillis()

                notesOnCache = notesOnCache + note

                noteList = noteList ::: List((Some(new ShortMessage(NOTE_ON, 2, note, 64)), 0l))
              }

              noteList
            }

            case _ => List((None, 0l))
          }

        }

        case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
          //println("Control change x: " + x.getData2);
          List((Some(new ShortMessage(PITCH_BEND, 2, 0, message.getData2 / 8)), 0l))
        }

        case _ => List((message, timeStamp))

      }

    }

  }

}