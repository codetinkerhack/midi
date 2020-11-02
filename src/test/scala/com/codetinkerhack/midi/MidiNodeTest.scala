package com.codetinkerhack.midi
import org.scalatest._
import flatspec._
import javax.sound.midi.{MidiMessage, ShortMessage}
import javax.sound.midi.ShortMessage.NOTE_ON
import matchers._
import CustomMatchers._

class MidiNodeTest extends AnyFlatSpec with should.Matchers {

  it should "send message via Output channel 1 of Node A to Input channel 1 of Node B" in {
    val node = MidiNode()
    val testNode = new TestMidiNode()

    val chain = node.out(1).connect(testNode.in(1))
    chain.send(new Message(new ShortMessage(NOTE_ON, 1, 1, 1),0, timeStamp = 0L))(null)

    val message = testNode.getLastMessage()
    message.asInstanceOf[Message] should matchMessage(new Message(new ShortMessage(NOTE_ON, 1, 1, 1), 0, timeStamp = 0L))
    message.getTimeStamp should equal (0L)
  }

  it should "route message via Output channel 1 of Node A to Input channel 2 of Node B" in {
    val node = MidiNode()
    val testNode = new TestMidiNode()

    val chain = node.out(1).connect(testNode.routeTo(2))
    chain.send(new Message(new ShortMessage(NOTE_ON, 1, 1, 1), 0, timeStamp = 0L))(null)

    val message = testNode.getLastMessage()
    message.asInstanceOf[Message] should matchMessage(new Message(new ShortMessage(NOTE_ON, 2, 1, 1), 0, timeStamp = 0L))
    message.getTimeStamp should equal (0L)
  }

  it should "transmit message to parallel chains only on specific channel. Message received on matching message channel" in {
    val node = MidiNode()
    val testNode = new TestMidiNode()
    val testNode1 = new TestMidiNode()

    val chain = node.out(1).connect(testNode)
    val chain1 = node.out(2).connect(testNode1)

    MidiParallel(chain, chain1).send(new Message(new ShortMessage(NOTE_ON, 1, 1, 1)))( null)

    val message = testNode1.getLastMessage()
    message should equal(null)
  }

  it should "transmit message to parallel chains on any channel. Message received output by both receiver" in {
    val node = MidiNode()
    val testNode = new TestMidiNode()
    val testNode1 = new TestMidiNode()

    val chain = node.connect(testNode)
    val chain1 = node.connect(testNode1)

    val message = new Message(new ShortMessage(NOTE_ON, 1, 1, 1))
    MidiParallel(chain, chain1).send(message)( null)

    val message1 = testNode.getLastMessage()
    message1 should equal(message)

    val message2 = testNode1.getLastMessage()
    message2 should equal(message)
  }
}
