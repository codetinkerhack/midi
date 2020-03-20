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
  private val Note_E1 = "E1"

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
