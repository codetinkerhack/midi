package com.codetinkerhack.toys

import javax.sound.midi._

import com.codetinkerhack.midi._
import com.codetinkerhack.midi.server.ComPortServer
import jssc.SerialPort


/**
  * Created by Evgeniy on 23/07/2017.
  */
object Evotar extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val comServer = new ComPortServer("COM5", SerialPort.BAUDRATE_19200,
      SerialPort.DATABITS_8,
      SerialPort.STOPBITS_1,
      SerialPort.PARITY_NONE)

    val output = mh.getReceivers.get("Real Time Sequencer")

    output.open()

    comServer.setReceiver(output.getReceiver);
    comServer.run();
    while(true) {}

  }
}

