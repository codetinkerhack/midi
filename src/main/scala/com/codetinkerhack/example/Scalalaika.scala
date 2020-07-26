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

    private val baseNote = 48

    private val scale = Array(0, 4, 7, 10, 16)

    private var currentChord: Chord = null

    private var timeLapsed = 0l
    private var notesOnCache = Set[Int]()


    override def processMessage(message: Option[MidiMessage], timeStamp: Long): List[(Option[MidiMessage], Long)] = {
      import ShortMessage._

      message match {

        case Some(m: MetaMessage) => {
          println(s"Chord received: ${  new String(m.getData()) }")

          val newChord = new Chord(new String(m.getData))

          if (currentChord != newChord) {

            currentChord = newChord

            val notesOff = notesOnCache.map(n => (Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0l))

            notesOnCache = Set.empty

            notesOff.toList  ::: ((message, 0L) :: List.empty)
          } else
            List((message, timeStamp))
        }

        case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 2 && currentChord != null) => {
          val ccy = message.getData2

          val note = baseNote + scale((128 - ccy) / 26)

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

            noteList = noteList ::: List((Some(new ShortMessage(NOTE_ON, 2, note, 64)), 10l))
          }

          noteList
        }

        case _ => List((message, timeStamp))

      }

    }

  }

}
