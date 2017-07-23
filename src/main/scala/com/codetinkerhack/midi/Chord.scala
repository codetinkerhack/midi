package com.codetinkerhack.midi

import java.util.{ArrayList, HashMap, List}

import scala.beans.BeanProperty

object Chord {

  val progressionMap = new HashMap[String, Array[Int]]()
  val noteToInt = new HashMap[String, Integer]()

  private var noteToIntMap: HashMap[String, Integer] = new HashMap[String, Integer]()
  private var intToNoteMap: HashMap[Integer, String] = new HashMap[Integer, String]()
  private val Note_C = "C"
  private val Note_Cx = "C#"
  private val Note_D = "D"
  private val Note_Dx = "D#"
  private val Note_E = "E"
  private val Note_F = "F"
  private val Note_Fx = "F#"
  private val Note_G = "G"
  private val Note_Gx = "G#"
  private val Note_A = "A"
  private val Note_Ax = "A#"
  private val Note_B = "B"
  private val Note_C1 = "C1"
  private val Note_C1x = "C1#"
  private val Note_D1 = "D1"
  private val Note_D1x = "D1#"
  private val Note_E1 = "E"

  progressionMap.put("min", Array(0, 2, 3, 5, 7, 9, 12))
  progressionMap.put("maj", Array(0, 2, 4, 5, 7, 9, 12))
  progressionMap.put("aug", Array(0, 2, 4, 5, 8, 9, 12))
  progressionMap.put("dim", Array(0, 2, 3, 5, 6, 9, 12))
  progressionMap.put("maj7", Array(0, 2, 4, 5, 7, 9, 11))
  progressionMap.put("7", Array(0, 2, 4, 5, 7, 9, 10))
  progressionMap.put("m7", Array(0, 2, 3, 5, 7, 9, 10))

  noteToInt.put("F#", 6)
  noteToInt.put("G ", -5)
  noteToInt.put("G#", -4)
  noteToInt.put("A ", -3)
  noteToInt.put("A#", -2)
  noteToInt.put("B ", -1)
  noteToInt.put("C ", 0)
  noteToInt.put("C#", 1)
  noteToInt.put("D ", 2)
  noteToInt.put("D#", 3)
  noteToInt.put("E ", 4)
  noteToInt.put("F ", 5)

  //  intToNoteMap.put(36, Note_C)
  //
  //  intToNoteMap.put(37, Note_Cx)
  //
  //  intToNoteMap.put(38, Note_D)
  //
  //  intToNoteMap.put(39, Note_Dx)
  //
  //  intToNoteMap.put(40, Note_E)
  //
  //  intToNoteMap.put(41, Note_F)
  //
  //  intToNoteMap.put(42, Note_Fx)
  //
  //  intToNoteMap.put(43, Note_G)
  //
  //  intToNoteMap.put(44, Note_Gx)
  //
  //  intToNoteMap.put(45, Note_A)
  //
  //  intToNoteMap.put(46, Note_Ax)
  //
  //  intToNoteMap.put(47, Note_B)
  //
  //  intToNoteMap.put(48, Note_C1)
  //
  //  intToNoteMap.put(49, Note_C1x)
  //
  //  intToNoteMap.put(50, Note_D1)
  //
  //  intToNoteMap.put(51, Note_D1x)
  //
  //  intToNoteMap.put(52, Note_E)
  //
  //  noteToIntMap.put(Note_C, 36)
  //
  //  noteToIntMap.put(Note_Cx, 37)
  //
  //  noteToIntMap.put(Note_D, 38)
  //
  //  noteToIntMap.put(Note_Dx, 39)
  //
  //  noteToIntMap.put(Note_E, 40)
  //
  //  noteToIntMap.put(Note_F, 41)
  //
  //  noteToIntMap.put(Note_Fx, 42)
  //
  //  noteToIntMap.put(Note_G, 43)
  //
  //  noteToIntMap.put(Note_Gx, 44)
  //
  //  noteToIntMap.put(Note_A, 45)
  //
  //  noteToIntMap.put(Note_Ax, 46)
  //
  //  noteToIntMap.put(Note_B, 47)
  //
  //  noteToIntMap.put(Note_C1, 48)
  //
  //  noteToIntMap.put(Note_C1x, 49)
  //
  //  noteToIntMap.put(Note_D1, 50)
  //
  //  noteToIntMap.put(Note_D1x, 51)
  //
  //  noteToIntMap.put(Note_E1, 52)

  def chordNoteReMap(chordFrom: Chord, chordTo: Chord, note: Int): Int = {
    val offset = chordTo.getChordBaseNote - chordFrom.getChordBaseNote
    val noteNormalized = note % 12
    val octave = Math.floor(note / 12).toInt
    val progressionNote = getProgressionNote(chordFrom, chordTo, noteNormalized)
    offset + progressionNote + (12 * octave)
  }

  def getProgressionNote(chordFrom: Chord, chordTo: Chord, note: Int): Int = {
    var i = 0
    while (i <= 6) {
      if (note == progressionMap.get(chordFrom.chordVariation)(i)) {
        return progressionMap.get(chordTo.chordVariation)(i)
      }
      i += 1
    }
    note
  }
}

/**
 * Created by Evgeniy on 15/06/2014.
 */
class Chord(val chord: String) {

  import Chord._

  @BeanProperty
  val chordBaseNote = noteToInt.get(chord.substring(0, 2))

  @BeanProperty
  val chordVariation = chord.substring(2).trim()

  def getChordNotes(): List[Integer] = {
    val list = new ArrayList[Integer]()

    println(s"chordBaseNote: $chordBaseNote chordVariation: '$chordVariation'")
    list.add(chordBaseNote)
    list.add(chordBaseNote + progressionMap.get(chordVariation)(2))
    list.add(chordBaseNote + progressionMap.get(chordVariation)(4))
    list.add(chordBaseNote + progressionMap.get(chordVariation)(6))
    list
  }

  override def toString(): String = chord

  def canEqual(other: Any): Boolean = other.isInstanceOf[Chord]

  override def equals(other: Any): Boolean = other match {
    case that: Chord =>
      (that canEqual this) &&
        chord == that.chord
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(chord)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
