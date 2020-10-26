package com.codetinkerhack.midi

import scala.collection.immutable.TreeMap


/**
  * Created by Evgeniy on 27/04/2015.
  */
class MidiDelay() extends MidiNode {

  var queue = new TreeMap[Long, (MidiMessageContainer, List[MidiNode])]()
  new Scheduler()

  def getCurrentTimeMillis(): Long = {
    System.nanoTime / 1000000l
  }


  class Scheduler() {

    var prevTime = getCurrentTimeMillis()

    new Thread() {
      override def run() {
        while (true) {

          val nowTime = getCurrentTimeMillis()

          if (nowTime >= prevTime + 1) {
            prevTime = nowTime

            queue.synchronized {
              val kv = queue.headOption

              kv foreach { v =>
                if (v._1 <= nowTime) {
                  println(s"Scheduler send ${v._1} and ${v._2}")
                  send(v._2._1, v._1, v._2._2)

                  queue = queue.tail
                }
              }
            }
          }
          else
            Thread.sleep(10)
        }
      }

    }.start()

  }

  override def processMessage(message: MidiMessageContainer, timeStamp: Long, chain: List[MidiNode]) {

    if (timeStamp == 0) {
      send(message, 0, chain)
    }
    else {
      queue.synchronized {
        queue += ((timeStamp + getCurrentTimeMillis) -> (message, chain))
      }
    }
  }

}
