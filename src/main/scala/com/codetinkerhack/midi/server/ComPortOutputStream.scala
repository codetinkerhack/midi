package com.codetinkerhack.midi.server

import java.io.OutputStream

import jssc.SerialPort
//remove if not needed

class ComPortOutputStream(var sp: SerialPort) extends OutputStream {

  override def write(b: Int) {
    sp.writeByte(b.toByte)
  }
}
