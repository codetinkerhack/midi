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
    val inputNanoPad = mh.getTransmitters.get("nanoPAD2")


    output.open()
    inputNanoPad.open()


    //input1.open()

    val chordAnalyzer = new ChordAnalyzer()
    val dummyReceiver = new DummyReceiver()
    val midiDelay = new MidiDelay()
    val scalaLika = getScalalaika()
    val chordTransformer = new ChordTransformer()
    val midiOut = MidiNode(output.getReceiver)
    val midiInNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))

    chordTransformer.setBaseChord(new Chord("E min"))


    val instrumentSelector = Scalalaika.getInstrumentSelector()

    // Scalalika
    midiInNanoPad.out(0).connect(instrumentSelector)

    instrumentSelector.out(0)
      .connect(chordAnalyzer).out(0)
      .connect(scalaLika).out(0)
      .connect(chordTransformer).out(0)
    //.connect(midiOut)

    instrumentSelector.out(1).connect(midiOut)
    instrumentSelector.out(2).connect(midiOut)

    scalaLika.out(1).connect(midiDelay).out(1).connect(chordTransformer).out(1).connect(midiOut)
    scalaLika.out(2).connect(chordTransformer).out(2).connect(midiOut)

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

          val notesOff = notesOnCache.seq.map(n => (Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0l))
          notesOnCache = Set.empty

          baseNoteOff :: notesOff.toList
        }

        case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 2) => {
          val ccy = message.getData2

          //println("Control change y: " + ccy)

          currentBaseNote match {
            case Some(m: ShortMessage) => {
              val note = baseNote + scale((128 - ccy) / 25)


              if (!notesOnCache(note) || (notesOnCache(note) && (currentTimeMillis() - timeLapsed) > 500)) {

                var notesOff = Set[(Some[ShortMessage], Long)]()
                if (currentTimeMillis() - timeLapsed > 100) {
                  notesOff = notesOnCache.seq.map(n => (Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0l))

                  notesOnCache = Set.empty
                  return notesOff.toList
                }

                timeLapsed = currentTimeMillis()

                notesOnCache = notesOnCache + note

                return List((Some(new ShortMessage(NOTE_ON, 2, note, 64)), 0l))
              }

              List((None, 0l))
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