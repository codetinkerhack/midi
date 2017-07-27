package com.codetinkerhack.midi.server

import java.io.InputStream

import jssc.{SerialPort, SerialPortException}

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
