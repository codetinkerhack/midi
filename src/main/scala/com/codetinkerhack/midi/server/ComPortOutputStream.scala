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

class ComPortOutputStream(var sp: SerialPort) extends OutputStream {

  override def write(b: Int) {
    sp.writeByte(b.toByte)
  }
}
