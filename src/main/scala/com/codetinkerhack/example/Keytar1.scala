package com.codetinkerhack.example

import javax.sound.midi._

import com.codetinkerhack.midi._


/**
  * Created by Evgeniy on 31/01/2015.
  */
object Keytar1 extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val output = mh.getReceivers.get("loopMIDI Port")
    val inputNanoPad = mh.getTransmitters.get("nanoPAD2")
    val inputNanoKey = mh.getTransmitters.get("nanoKEY2")

    output.open()
    inputNanoPad.open()
    inputNanoKey.open()

    val chordAnalyzer = new ChordAnalyzer()
    val midiDelay = new MidiDelay()
    val keytar = new Keytar1()
    val chordTransformer = new ChordTransformer()
    val midiOut = MidiNode(output.getReceiver)
    val midiInNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))

    val midiInNanoKey = MidiNode(inputNanoKey.getTransmitters.get(0))

    chordTransformer.setBaseChord(new Chord("E min"))

    val instrumentSelector = new MidiFunction((message: Option[MidiMessage], timeStamp: Long) => {

      val baseInstrument = IndexedSeq(26, 30, 5, 7)
      val soloInstrument = IndexedSeq(26, 29, 10, 40)

      import javax.sound.midi.ShortMessage._

      message match {
        case Some(m: ShortMessage) if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
          var messageList = List[(Option[ShortMessage], Long)]()

          messageList = (Some(new ShortMessage(PROGRAM_CHANGE, 1, baseInstrument((m.getData1 - 36) / 16), 0)), 0l) :: messageList
          messageList = (Some(new ShortMessage(PROGRAM_CHANGE, 2, soloInstrument((m.getData1 - 36) / 16), 0)), 0l) :: messageList
          messageList = (Some(new ShortMessage(m.getCommand, m.getChannel, (m.getData1 - 36) % 16, 0)), 0l) :: messageList

          messageList
        }
        case _ => List((message, timeStamp))
      }
    })


    midiInNanoPad.out(0).connect(instrumentSelector)

    midiInNanoKey.out(0).connect(new Router(2)).out(2).connect(keytar)

    instrumentSelector.out(0)
      .connect(chordAnalyzer).out(0)
      .connect(keytar).out(0)
      .connect(chordTransformer).out(0)

    instrumentSelector.out(1).connect(midiOut)
    instrumentSelector.out(2).connect(midiOut)

    keytar.out(1).connect(midiDelay).out(1).connect(chordTransformer).out(1).connect(midiOut)
    keytar.out(2).connect(chordTransformer).out(2).connect(midiOut)
  }
}

class Keytar1() extends MidiNode {


  //    Emin
  //    4, 11, 16, 19, 23, 36
  private val baseNote = 40

  private val blackNotes = Array[Int](0, 7, 0 , 12, 0 , 0 , 21, 0 , 24, 0 )
  private val whiteNotes = Array[Int](4, 0, 11, 0 , 16, 19, 0 , 23, 0 , 28)
  private val scale = (blackNotes zip whiteNotes).map( x=> x._1 + x._2)

  private var currentBaseNote: Option[ShortMessage] = None

  private var timeLapsed: Long = 0;
  private var notesOnCache = Set[Int]()
  private var currentChord: Option[Chord] = None


  private def updateChord(chord: Chord): Unit = {currentChord = Some(chord)}

  override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    import ShortMessage._

    message match {

      case Some(m: MetaMessage) => {

        println(s"Keytar Chord received: ${  new String(m.getData()) }")

        updateChord(new Chord(new String(m.getData)))

        send(Some(m),0)
      }

      case Some(message: ShortMessage) if (message.getCommand == NOTE_ON && message.getChannel == 0) => {

        val scaleNote = currentChord match {
          case Some(chord) => if(chord.chordVariation == "maj")
                                  scale(2)
                                else
                                  scale(0)
          case _ => scale(0)
        }

        val note = baseNote + scaleNote - 12
        currentBaseNote = Some(new ShortMessage(NOTE_ON, 1, note, 64))
        send(Some(new ShortMessage(PITCH_BEND, 1, 0, 0)), 0)
        send(currentBaseNote, 60)
      }

      case Some(message: ShortMessage) if (message.getCommand == NOTE_OFF && message.getChannel == 0) => {
        currentBaseNote foreach (n => send(Some(new ShortMessage(NOTE_OFF, 1, n.getMessage()(1), 0)), 0))
        notesOnCache.seq foreach (n => send(Some(new ShortMessage(NOTE_OFF, 2, n, 0)), 0))
        notesOnCache = Set.empty
      }

      case Some(message: ShortMessage) if message.getCommand == NOTE_ON && message.getChannel == 2 => {
        val ccy = message.getData1

       // println("Control change y: " + ccy)

        currentBaseNote match {
          case Some(m: ShortMessage) => {
            val note = baseNote + scale((ccy - 48))
            send(Some(new ShortMessage(NOTE_ON, 2, note, message.getData2)), 0)
            notesOnCache = notesOnCache + note
          }
          case _ =>
        }
      }
      case Some(message: ShortMessage) if (message.getCommand == NOTE_OFF && message.getChannel == 2) => {
        val ccy = message.getData1

        // println("Control change y: " + ccy)

        currentBaseNote match {
          case Some(m: ShortMessage) => {
            val note = baseNote + scale((ccy - 48))
            send(Some(new ShortMessage(NOTE_OFF, 2, note, 0)), 0)
            notesOnCache = notesOnCache + note
          }

          case _ =>
        }
      }

      case Some(message: ShortMessage) if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
        //println("Control change x: " + x.getData2);
        send(Some(new ShortMessage(PITCH_BEND, 2, 0, message.getData2 / 8)), 0)
      }

      case _ => {

        send(message, timeStamp)
      }
    }
  }

}