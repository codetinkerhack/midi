package com.codetinkerhack.midi

import java.util.HashSet

import javax.sound.midi._

import scala.collection.immutable.List

class ChordModifier extends MidiNode {

  private val CHORD_DONE = "CHORD_DONE"
  private val CHORD_READING = "CHORD_READING"
  private val notesOn = new HashSet[MidiMessageContainer]
  var state: String = CHORD_DONE
  var notesStash = List[MidiMessageContainer]()
  private var baseChord = new Chord("C 7")
  private var currentChord = new Chord("C 7")

  def setBaseChord(c: Chord) {
    baseChord = c
  }

  def getCurrentChord(): Chord = currentChord

  override def processMessage(message: MidiMessageContainer, send: MidiMessageContainer => Unit): Unit = {

    notesOn.synchronized {
      message.get match {
        case m: MetaMessage if m.getType == 2 => {

          println(s"Chord received: ${new String(m.getData())}")

          updateChord(message.getChord, send)
          state = CHORD_DONE

          println(s"Stash queue size: " + notesStash.size)
          notesStash.reverse.foreach(n => {
            val m = n.get.asInstanceOf[ShortMessage]
            val transposedMessage = new ShortMessage(m.getStatus, Chord.chordNoteReMap(n.getChord, currentChord,
               m.getData1), m.getData2)
            notesOn.add(new MidiMessageContainer(transposedMessage, chord = currentChord))
            send(new MidiMessageContainer(transposedMessage, chord = currentChord))
          })
          notesStash = List()

        }
        case m: MetaMessage if m.getType == 1 => {
          println(s"Chord being read")
          state = CHORD_READING
        }
        case m: ShortMessage => {

          try {
            // For all non percussion notes on / off
            if (m.getChannel != 9 && m.getChannel != 8 && (m.getCommand == ShortMessage.NOTE_ON || m.getCommand == ShortMessage.NOTE_OFF)) {

              val transposedMessage = new ShortMessage(m.getStatus, Chord.chordNoteReMap(baseChord, message.getChord,
                m.getData1), m.getData2)

              if (state == CHORD_DONE) {
                notesOn.add(new MidiMessageContainer(transposedMessage, chord = message.getChord))
                send(new MidiMessageContainer(transposedMessage, chord = message.getChord))
              }
              else if (state == CHORD_READING && m.getCommand == ShortMessage.NOTE_OFF) {
                notesOn.remove(new MidiMessageContainer(transposedMessage, chord = message.getChord))
                send(new MidiMessageContainer(transposedMessage, chord = message.getChord).getNoteOff())
              }
              else if (state == CHORD_READING && m.getCommand == ShortMessage.NOTE_ON) {
                println(s"Stashing")
                notesStash = message :: notesStash
              }

            } else if (m.getChannel == 9 || m.getChannel == 8) { // Drums
              send(message)
            } else if (m.getCommand != ShortMessage.NOTE_ON && m.getCommand != ShortMessage.NOTE_OFF) {
              // println(s"Other non noteon/off length: ${m.getMessage.length}, NoteOn: ${m.getCommand == ShortMessage.NOTE_ON}, NoteOff: ${m.getCommand == ShortMessage.NOTE_OFF}, ${m.getChannel}, ${m.getData1},  ${m.getData2}")
              send(message)
            }
          } catch {
            case e: InvalidMidiDataException => {
              e.printStackTrace()
            }
          }
        }
        case _ => {
          // println("Other message received");
          send(message)
        }
      }
    }
  }

  private def updateChord(newChord: Chord, send: MidiMessageContainer => Unit) = {
    notesOn.synchronized {
      if (newChord != Chord.NONE && newChord != currentChord && newChord != null) {
        notesTranspose(currentChord, newChord, send)
        this.currentChord = newChord
      }
    }
  }

  private def notesTranspose(oldChord: Chord, newChord: Chord, send: MidiMessageContainer => Unit) {

    println(s"Currently on: ${notesOn.size}")

    notesOn.forEach(m => {
      try {
        send(m.getNoteOff())

        val message = m.get.asInstanceOf[ShortMessage]
//        val onMessage =  new MidiMessageContainer(new ShortMessage(ShortMessage.NOTE_ON, message.getChannel, Chord.chordNoteReMap(m.getChord,
//                          newChord, message.getData1), message.getData2), m.getDepth, chord = newChord)
//
//        send(onMessage, chain)
      } catch {
        case ex: Exception =>  {
          ex.printStackTrace()
        }
      }
    })
    notesOn.clear()
  }


}
