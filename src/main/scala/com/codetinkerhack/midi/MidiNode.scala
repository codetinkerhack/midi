package com.codetinkerhack.midi

import java.security.DrbgParameters.NextBytes

import javax.sound.midi._

import scala.collection.immutable._

/**
  * Created by Evgeniy on 26/04/2015.
  */
object MidiNode {

  def apply(): MidiNode = apply("")
  def apply(name: String): MidiNode = MidiNode((message, timeStamp) => List((message, timeStamp)))

  def apply(func: (MidiMessageContainer, Long) => List[(MidiMessageContainer, Long)]): MidiNode = apply("", func)
  def apply(name: String, func: (MidiMessageContainer, Long) => List[(MidiMessageContainer, Long)]) = {
    new MidiNode() {

      override def getName() = {
        name
      }

      override def processMessage(message: MidiMessageContainer, timestamp: Long, chain: List[MidiNode]): Unit = {
        func(message, timestamp).foreach( m => super.send(m._1, m._2, chain))
      }
    }
  }

  def apply(transmitter: Transmitter) = {

    val midiNode = new MidiNode() {
      var next: MidiNode = null

      override def getName(): String = "Start"

      override def connect(next: MidiNode): MidiNode = {
        this.next = next
        this
      }

      override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]) {
        next.processMessage(message, timeStamp, null)
      }
    }

    transmitter.setReceiver(new Receiver {

      override def send(message: MidiMessage, timeStamp: Long): Unit = {
        try {
          midiNode.processMessage(new MidiMessageContainer(message), timeStamp, null)
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
      override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]): Unit = {
        receiver.send(message.get, timeStamp)
        List()
      }
    }

    midiNode
  }
}

trait MidiNode {

  def log(str: String, message: MidiMessageContainer): Unit = {
    (1L to message.getDepth).foreach(_ => printf("\t"))
    println(str)
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

  final def receive(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]) {
    log(s"Node: ${this.getName}", message)
    processMessage(message, timeStamp, chain)
  }

  def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]) {
    send(message, timeStamp, chain)
  }

  def send(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]) {
    if(chain.nonEmpty) {
      val newMessage = message.clone()
      newMessage.incDepth()
      chain.head.receive(newMessage, timeStamp, chain.tail)
    }
  }


  def close(): Unit = this.close()
}