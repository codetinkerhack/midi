package com.codetinkerhack.midi

import scala.collection.immutable.TreeMap


/**
  * Created by Evgeniy on 27/04/2015.
  */
class MidiDelay() extends MidiNode {

  var queue = new TreeMap[Long, (Message, Message => Unit)]()

  MidiNode.register1MsTimedHandler(() => {
    queue.synchronized {
      val kv = queue.headOption

      kv foreach { v =>
        if (v._1 <= MidiNode.getCurrentTimeMillis()) {

          val (send, message) = (v._2._2, v._2._1)
          send(message)

          queue = queue.tail
        }
      }
    }
  })

  override def processMessage(message: Message, send: Message => Unit): Unit = {

    if (message.getTimeStamp == 0) {
      send(message)
    }
    else {
      queue.synchronized {
        queue += ((message.getTimeStamp + MidiNode.getCurrentTimeMillis) -> (message, send))
      }
    }
  }

}
