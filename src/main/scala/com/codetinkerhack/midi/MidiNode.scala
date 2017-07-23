package com.codetinkerhack.midi

import javax.sound.midi._

import scala.collection.immutable._

/**
  * Created by Evgeniy on 26/04/2015.
  */
object MidiNode {

  //  implicit class ExtendedReceiver (x:Transmitter) {
  //    def +> [T <: ReceiverTransmitter](y:T): T  = {
  //      x.setReceiver(y)
  //      y
  //    }
  //  }


  def apply(x: Transmitter) = {

    val midiNode = new MidiNode() {}

    x.setReceiver(new Receiver {

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

  def apply(x: Receiver) = {

    val midiNode = new MidiNode {
      override def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
        message foreach { m => x.send(m, timeStamp) }
        //this.send(message, timeStamp)
      }
    }

    midiNode
  }
}

trait MidiNode {
  val OUT_1 = 1
  val OUT_2 = 2

  val BROADCAST_CHANNEL: Int = 100
  private var channels = Map[Int, MidiChannel]()

  case class MidiChannel(val channel: Int) {

    var midiNodes: Set[MidiNode] = Set.empty

    def connect(node: MidiNode): MidiNode = {
      midiNodes = midiNodes + node
      node
    }

    def send(message: Option[MidiMessage], timeStamp: Long): Unit = {
      midiNodes foreach (_.receive(message, timeStamp))
    }
  }

  def out(channel: Int): MidiChannel = channels.get(channel) match {
    case Some(midiChannel) => midiChannel
    case None => {
      val midiChannel = MidiChannel(channel)
      channels = channels + (channel -> midiChannel)
      midiChannel
    }
  }

  def receive(message: Option[MidiMessage], timeStamp: Long): Unit = {
    send(message, timeStamp)
  }

  final def send(message: Option[MidiMessage], timeStamp: Long): Unit = {

    message match {
      case Some(m: ShortMessage) => {

        out(m.getChannel).send(message, timeStamp)
        //getChannel(BROADCAST_CHANNEL).send(message, timeStamp)

      }

      case Some(m: MetaMessage) =>
          out(0).send(message, timeStamp)


      case _ => out(BROADCAST_CHANNEL).send(message, timeStamp)

    }
  }


  def close(): Unit = {}

}

