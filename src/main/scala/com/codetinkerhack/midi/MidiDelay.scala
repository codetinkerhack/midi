package com.codetinkerhack.midi

import scala.collection.immutable.TreeMap


/**
  * Created by Evgeniy on 27/04/2015.
  */
class MidiDelay() extends MidiNode {

  var queue = new TreeMap[Long, (MMessage, MMessage => Unit)]()
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
                  v._2._2(v._2._1)

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

  override def processMessage(message: MMessage, send: MMessage => Unit): Unit = {

    if (message.getTimeStamp == 0) {
      send(message)
    }
    else {
      queue.synchronized {
        queue += ((message.getTimeStamp + getCurrentTimeMillis) -> (message, send))
      }
    }
  }

}
