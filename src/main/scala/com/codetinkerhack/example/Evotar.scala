package com.codetinkerhack.example

import com.codetinkerhack.midi._
import com.codetinkerhack.midi.server.ComPortServer
import jssc.SerialPort


/**
  * Created by Evgeniy on 23/07/2017.
  */
object Evotar extends App {

  override def main(args: Array[String]) {

    val mh = new MidiHandler()
    val comServer = new ComPortServer("COM5", SerialPort.BAUDRATE_115200,
      SerialPort.DATABITS_8,
      SerialPort.STOPBITS_1,
      SerialPort.PARITY_NONE)

    val output = mh.getReceivers.get("Gervill")

    output.open()

    comServer.setReceiver(output.getReceiver);
    comServer.run();
    while(true) {}

  }
}
