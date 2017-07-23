package com.codetinkerhack.toys

import javax.sound.midi._

import com.codetinkerhack.midi._

/**
  * Created by Evgeniy on 9/05/2015.
  */
object Harmonica extends App {

  def midiOnToOff(message: Option[ShortMessage]): Option[ShortMessage] = {
    import ShortMessage._

    message map (note => (new ShortMessage(NOTE_OFF, 0, note.getData1, 64)))
  }

  override def main(args: Array[String]) {


    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopMIDI Port")
    // val input = mh.getTransmitters.get("nanoPAD2")
    val input1 = mh.getTransmitters.get("APC Key 25")

    output.open()
    input1.open()


    // import ExtendedReceiver._


    val chordAnalyzer = new ChordAnalyzer()
    val dummyReceiver = new DummyReceiver()
    // val midiDelay = new MidiDelay()
    val harmonica = new Harmonica()
    val chordTransformer = new ChordTransformer()

    val selectInstrument = new MidiFunction((message: Option[MidiMessage], timeStamp: Long) => {

      val baseInstrument = IndexedSeq(26, 30, 5, 7)
      val soloInstrument = IndexedSeq(24, 29, 10, 40)

      import javax.sound.midi.ShortMessage._

      message match {
        case Some(m: ShortMessage) if ((m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) && m.getChannel == 0) => {
          // println(s"Note: ${m.getData1} Channel: ${m.getChannel}")

          var messagesList = List[(Option[ShortMessage], Long)]()

          messagesList = (Some(new ShortMessage(PROGRAM_CHANGE, 0, baseInstrument(0), 0)), 0l) :: messagesList
          messagesList = (Some(new ShortMessage(m.getCommand, 0, m.getData1, 64)), 0l) :: messagesList

          messagesList
        }
        case Some(m: ShortMessage) if ((m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) && m.getChannel == 1) => {

          var messagesList = List[(Option[ShortMessage], Long)]()

          messagesList = (Some(new ShortMessage(PROGRAM_CHANGE, 1, soloInstrument(0), 0)), 0l) :: messagesList
          messagesList = (Some(new ShortMessage(m.getCommand, 1, m.getData1, m.getData2)), 0l) :: messagesList

          messagesList
        }
        case _ => List((message, timeStamp))
      }
    })

    //    Akai 25

    val outputMidi = MidiNode(output.getReceiver)
    val inputMidi = MidiNode(input1.getTransmitters.get(0))

    inputMidi.out(0)
      .connect(selectInstrument).out(0)
      .connect(chordAnalyzer).out(0)
      .connect(harmonica).out(0)
      .connect(chordTransformer).out(0)
      .connect(outputMidi)

    inputMidi.out(1)
      .connect(selectInstrument).out(1)
      .connect(harmonica).out(1)
      .connect(outputMidi)

  }


  class Harmonica() extends MidiNode {


    //    Emin
    //    4, 11, 16, 19, 23, 36

    private val baseNote = 24

    //private val scale = Array(4, 11, 16, 19, 23, 28)
    private val scale = Array(0, 2, 4, 5, 7, 12, 14, 16, 17, 19, 22, 24, 28)

    private var prevNoteOff: Option[ShortMessage] = None
    private var currentBaseNote: Option[ShortMessage] = None

    private var prevNote = -1
    private var timeLapsed: Long = 0;
    private var notesOnCache = Set[Int]()


    override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
      import ShortMessage._

      message match {

        case Some(m: MetaMessage) => {

          send(midiOnToOff(currentBaseNote), 0)

          val note = baseNote + 12;
          // + scale(0) - 12
          currentBaseNote = Some(new ShortMessage(NOTE_ON, 0, note, 64))

          send(message, 0)
          send(Some(new ShortMessage(PITCH_BEND, 0, 0, 0)), 0)
          send(Some(new ShortMessage(PITCH_BEND, 1, 0, 0)), 0)
          send(currentBaseNote, 0)

        }
        case Some(message: ShortMessage) if (message.getCommand == NOTE_OFF && message.getChannel == 0) => {

          send(midiOnToOff(currentBaseNote), 0)

        }

        case Some(message: ShortMessage) if (message.getCommand == NOTE_ON && message.getChannel == 1) => {
          val ccy = message.getData1


          val octave = (ccy) / 12
          val note = baseNote + ccy //scale((ccy) % 12) + 12 * octave

          if (!notesOnCache(note)) notesOnCache = notesOnCache + note

          send(Some(new ShortMessage(NOTE_ON, 1, note, message.getData2)), 0)
        }

        case Some(message: ShortMessage) if (message.getCommand == NOTE_OFF && message.getChannel == 1) => {
          val ccy = message.getData1

          //println("Control change y: " + ccy)

          val octave = (ccy) / 12
          val note = baseNote + ccy //scale((ccy) % 12) + 12 * octave

          if (notesOnCache(note)) notesOnCache = notesOnCache - note


          send(Some(new ShortMessage(NOTE_OFF, 1, note, 0)), 0)
        }
        //        case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
        //          //println("Control change x: " + x.getData2);
        //          receiver.send(Some(new ShortMessage(PITCH_BEND, 2, 0, message.getData2 / 8)), 0)
        //        }

        //      case Some(message: MetaMessage) => {
        //
        //        receiver.send(Some(message), timeStamp)
        //      }

        // case Some(m:MetaMessage) =>   send(message, timeStamp)

        case Some(m: ShortMessage) if (m.getCommand == PROGRAM_CHANGE) => send(message, timeStamp)

        case _ =>
      }
    }

  }

}
