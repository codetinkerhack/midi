package com.codetinkerhack.midi.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.HashMap
//remove if not needed
import scala.collection.JavaConversions._

class ChecksumAckStream(var is: InputStream, var os: OutputStream) extends InputStream {

  var packetReceived: HashMap[Byte, Byte] = new HashMap[Byte, Byte]()

  var readingState: Boolean = true

  var currentByte: Int = 0

  var data: Array[Byte] = new Array[Byte](6)

  var num: Int = 0

  override def read(): Int = {
    if (readingState) {
      do {
        num = 0
        var b = 0
        while (b != 254) {
          num += 1
          b = is.read() & 0xFF
        }
        if (num > 1) println(" $num bytes skipped! \n", num)
        num = 0
        data(0) = is.read().toByte
        data(1) = is.read().toByte
        data(2) = is.read().toByte
        data(3) = is.read().toByte
        data(4) = is.read().toByte
        data(5) = is.read().toByte
        if ((((data(1) + data(2) + data(3)) & 0xFF).toInt != (data(4) & 0xFF).toInt)) {
          println("Bad checksum!")
        }
      } while ((((data(1) + data(2) + data(3)) & 0xFF).toInt != (data(4) & 0xFF).toInt) || 
        (packetReceived.get(data(0)) != null && packetReceived.get(data(0)) == data(4)));
      readingState = false
      currentByte = 2
      packetReceived.put(data(0), data(4))
      return data(1) & 0xFF
    } else {
      if (currentByte < 4) {
        if (currentByte + 1 == 4) readingState = true
        currentByte=currentByte+1
        return data(currentByte) & 0xFF
      }
    }
    0
  }
}
