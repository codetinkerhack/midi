//package com.codetinkerhack.midi
//
//import java.util.HashSet
//
//import javax.sound.midi._
//
//import scala.collection.JavaConversions._
//
//class SimpleChordModifier extends MidiNode {
//
//  def setBaseChord(c: Chord) {
//    baseChord = c
//  }
//
//  private var baseChord = new Chord("C maj")
//
//  private var currentChord = new Chord("C maj")
//
//  private val notesOn = new HashSet[ShortMessage]
//
//  private def updateChord(newChord: Chord) {
//    notesOn.synchronized {
//      if (newChord != currentChord && newChord != null) {
//        //this.newChord = newChord
//        notesTranspose(currentChord, newChord)
//        this.currentChord = newChord
//        println(newChord)
//      }
//    }
//  }
//
//  def getCurrentChord(): Chord = currentChord
//
//  private def notesTranspose(oldChord: Chord, newChord: Chord) {
//
//    println(s"Currently on: ${notesOn.size}")
//
//    for (m <- notesOn) {
//      try {
//
//        val offMessage = new ShortMessage(ShortMessage.NOTE_OFF, m.getChannel, oldChord.getChordBaseNote + m.getData1, 0)
//
//        send(offMessage, 0)
//
//        val onMessage = new ShortMessage(ShortMessage.NOTE_ON, m.getChannel, newChord.getChordBaseNote + m.getData1, m.getData2)
//
//        send(onMessage, 0)
//
//      } catch {
//        case ex: Exception => ex.printStackTrace()
//      }
//    }
//  }
//
//  override def receive(message: MidiMessage, timeStamp: Long, depth: Long) {
//    notesOn.synchronized {
//      message match {
//        case m: MetaMessage if m.getType == 2 => {
//
//          println(s"Chord received: ${new String(m.getData())}")
//
//          updateChord(new Chord(new String(m.getData)))
//        }
//        case m: ShortMessage => {
//
//          try {
//            // Do not transpose Drum / percussion channels
//            if (m.getMessage.length == 3 && m.getChannel != 9 && m.getChannel != 8 &&
//              ((m.getCommand == ShortMessage.NOTE_ON
//                && !notesOn.contains(new MidiMessageHD(m))
//                ) || (m.getCommand == ShortMessage.NOTE_OFF
//                // && notesOn.contains(new HashMidiMessage(m))
//                )
//                )) {
//
//              val transposedMessage = new ShortMessage(m.getStatus, currentChord.getChordBaseNote + m.getData1, m.getData2)
//
//              send(transposedMessage, timeStamp)
//
//              if (m.getCommand == ShortMessage.NOTE_ON)
//                notesOn.add(new MidiMessageHD(m))
//              else
//                notesOn.remove(new MidiMessageHD(m))
//
//              // println(s"Currently on: ${notesOn.size}")
//
//            } else if (m.getChannel == 9 || m.getChannel == 8) {
//              send(m, timeStamp)
//            } else if (m.getCommand == ShortMessage.NOTE_ON || m.getCommand == ShortMessage.NOTE_OFF) {
//              println(s"Other noteon/off length: ${m.getMessage.length}, NoteOn: ${m.getCommand == ShortMessage.NOTE_ON}, NoteOff: ${m.getCommand == ShortMessage.NOTE_OFF}, ${m.getChannel}, ${m.getData1},  ${m.getData2}, ${timeStamp}")
//              //receiver.send(m, timeStamp)
//            }
//            else if (m.getCommand != ShortMessage.NOTE_ON && m.getCommand != ShortMessage.NOTE_OFF) {
//              // println(s"Other non noteon/off length: ${m.getMessage.length}, NoteOn: ${m.getCommand == ShortMessage.NOTE_ON}, NoteOff: ${m.getCommand == ShortMessage.NOTE_OFF}, ${m.getChannel}, ${m.getData1},  ${m.getData2}")
//              send(m, timeStamp)
//            }
//          } catch {
//            case e: InvalidMidiDataException => e.printStackTrace()
//          }
//        }
//        case _ => {
//          // println("Other message received");
//          send(message, timeStamp)
//        }
//      }
//    }
//  }
//
//
//}
