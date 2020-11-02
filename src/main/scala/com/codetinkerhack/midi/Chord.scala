package com.codetinkerhack.midi

import java.util.HashMap

import scala.beans.BeanProperty

object Chord {
  val scaleMap = new HashMap[String, Array[Int]]()
  val noteToInt = new HashMap[String, Integer]()

  val NONE = new Chord("N N")

  scaleMap.put("min", Array(0, 2, 3, 5, 7, 9, 12))
  scaleMap.put("maj", Array(0, 2, 4, 5, 7, 9, 12))
  scaleMap.put("aug", Array(0, 2, 4, 5, 8, 9, 12))
  scaleMap.put("dim", Array(0, 2, 3, 5, 6, 9, 12))
  scaleMap.put("maj7", Array(0, 2, 4, 5, 7, 9, 11))
  scaleMap.put("7", Array(0, 2, 4, 5, 7, 9, 10))
  scaleMap.put("m7", Array(0, 2, 3, 5, 7, 9, 10))

  noteToInt.put(NONE.chord, -100)
  noteToInt.put("B ", -1)
  noteToInt.put("C ", 0)
  noteToInt.put("C#", 1)
  noteToInt.put("D ", 2)
  noteToInt.put("D#", 3)
  noteToInt.put("E ", 4)
  noteToInt.put("F ", 5)
  noteToInt.put("F#", 6)
  noteToInt.put("G ", 7)
  noteToInt.put("G#", 8)
  noteToInt.put("A ", 9)
  noteToInt.put("A#", 10)

  def chordNoteReMap(chordFrom: Chord, chordTo: Chord, note: Int): Int = {
    val offset = chordTo.getChordBaseNote - chordFrom.getChordBaseNote
    val noteNormalized = note % 12
    val octave = Math.floor(note / 12).toInt
    val scaleNote = getScaleNote(chordFrom, chordTo, noteNormalized)
    offset + scaleNote + (12 * octave)
  }

  def getScaleNote(chordFrom: Chord, chordTo: Chord, note: Int): Int = {
    var i = 0
    while (i <= 6) {
      if (note == scaleMap.get(chordFrom.chordType)(i)) {
        return scaleMap.get(chordTo.chordType)(i)
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
  val chordType = chord.substring(2).trim()

  def getScale = scaleMap.get(chordType)

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
