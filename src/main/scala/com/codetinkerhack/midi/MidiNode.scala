package com.codetinkerhack.midi

import javax.sound.midi._

import scala.collection.immutable._

/**
  * Created by Evgeniy on 26/04/2015.
  */
object MidiNode {

  def apply(func: (Option[MidiMessage], Long) => List[(Option[MidiMessage], Long)]) = {
    new MidiNode() {
      override def processMessage(message: Option[MidiMessage], timestamp: Long): List[(Option[MidiMessage], Long)] = {
        func(message, timestamp)
      }
    }
  }

  def apply(transmitter: Transmitter) = {

    val midiNode = new MidiNode() {}

    transmitter.setReceiver(new Receiver {

      override def send(message: MidiMessage, timeStamp: Long): Unit = {
        try {
          midiNode.receive(Some(message), timeStamp)
        }
        catch {
          case e: Exception => println(e.printStackTrace())
        }
      }

      override def close(): Unit = midiNode.close()
    })

    midiNode
  }

  def apply(receiver: Receiver) = {

    val midiNode = new MidiNode {
      override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
        message foreach { m => receiver.send(m, timeStamp) }
      }
    }

    midiNode
  }
}

trait MidiNode {
  val OUT_1 = 1
  val OUT_2 = 2

  val BROADCAST_CHANNEL: Int = 100
  private var nodes = Set[MidiNode]()

  def connect(node: MidiNode): MidiNode = {
    nodes = nodes + node
    node
  }

  def processMessage(message: Option[MidiMessage], timeStamp: Long): List[(Option[MidiMessage], Long)] = List((message, timeStamp))

  def in(channel: Int): MidiNode = ChannelRouter(channel).connect(this)

  def out(channel: Int): MidiNode = this.connect(ChannelFilter(channel))

  def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    processMessage(message, timeStamp).foreach(m => send(m._1, m._2))
  }

  final def send(message: Option[MidiMessage], timeStamp: Long): Unit = {
    nodes.foreach( _.receive(message, timeStamp))
  }

  def close(): Unit = nodes.foreach( _.close())

}