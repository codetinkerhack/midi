package com.codetinkerhack.midi

import com.codetinkerhack.midi.MidiNode.DEBUG
import javax.sound.midi._

import scala.collection.immutable._

/**
  * Created by Evgeniy on 26/04/2015.
  */
object MidiNode {
  val DEBUG = false

  def apply(): MidiNode = apply("")
  def apply(name: String): MidiNode = MidiNode(message => List(message))

  def apply(func: (MMessage) => List[MMessage]): MidiNode = apply("", func)
  def apply(name: String, func: MMessage => List[MMessage]) = {
    new MidiNode() {

      override def getName() = {
        name
      }

      override def processMessage(message: MMessage, send: MMessage => Unit): Unit = {
        func(message).foreach(send(_))
      }
    }
  }

  def apply(transmitter: Transmitter) = {

    val midiNode = MidiChain()

    transmitter.setReceiver(new Receiver {

      override def send(message: MidiMessage, timeStamp: Long) {
        try {
          midiNode.send(new MMessage(message, timeStamp = timeStamp))(null)
        }
        catch {
          case e: Exception => println(e.printStackTrace())
        }
      }

      override def close() = midiNode.close()
    })

    midiNode
  }

  def apply(receiver: Receiver) = {

    val midiNode = new MidiNode {
      override def processMessage(message: MMessage, send: MMessage => Unit): Unit = {
        receiver.send(message.get, message.getTimeStamp)
      }
    }

    midiNode
  }
}

trait MidiNode {

  def log(str: String, message: MMessage): Unit = {
    if (DEBUG) {
      (1L to message.getDepth).foreach(_ => printf("\t"))
      println(str)
    }
  }

  def getName() = {
    this.getClass.getSimpleName
  }

  def connect(node: MidiNode): MidiNode = {
    MidiChain().connect(this).connect(node)
  }

  final def in(channel: Int): MidiNode = {
    val node = ChannelFilter(channel)
    node.connect(this)
  }

  final def routeTo(channel: Int): MidiNode = {
    val node = ChannelRouter(channel)
    node.connect(this)
  }

  final def out(channel: Int): MidiNode = {
    val node = ChannelFilter(channel)
    this.connect(node)
  }

  final def receive(message: MMessage, chain: List[MidiNode]) {
    log(s"Node: ${this.getName}", message)
    processMessage(message, message => this.send(message)(chain))
  }

  def processMessage(message: MMessage, send: MMessage => Unit): Unit = {
    send(message)
  }

  def send(message: MMessage)(chain: List[MidiNode]) {
    if(chain != null && chain.nonEmpty) {
      val newMessage = message.clone()
      newMessage.incDepth()
      chain.head.receive(newMessage, chain.tail)
    }
  }

  def close(): Unit = this.close()
}