package com.codetinkerhack.midi.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import jssc.SerialPort
import jssc.SerialPortException
//remove if not needed
import scala.collection.JavaConversions._

class ComPortInputStream(var sp: SerialPort) extends InputStream {

  override def read(): Int = {
    try {
      val b = sp.readBytes(1)(0)
      return b & 0xFF
    } catch {
      case e: SerialPortException => e.printStackTrace()
    }
    -1
  }
}
