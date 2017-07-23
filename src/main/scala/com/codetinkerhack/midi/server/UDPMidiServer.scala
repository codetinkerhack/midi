package com.codetinkerhack.midi.server

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import javax.sound.midi.InvalidMidiDataException
import javax.sound.midi.ShortMessage
import javax.sound.midi.Transmitter
//remove if not needed
import scala.collection.JavaConversions._

class UDPMidiServer() {

  private val port = 6789

  @volatile private var keepLooping: Boolean = true

  private var transmitter: Transmitter = _

  def setTransmitter(trns: Transmitter) {
    this.transmitter = trns
  }

  var cw: ConnectionThread = null

  try {
    cw = new ConnectionThread()
    new Thread(cw).start()
  } catch {
    case e: SocketException => e.printStackTrace()
  }

  class ConnectionThread() extends Runnable {

    protected var socket: DatagramSocket = new DatagramSocket(port)

    override def run() {
      val data = Array.ofDim[Byte](3)
      var mm: ShortMessage = null
      while (keepLooping) {
        try {
          data(0) = 0
          data(1) = 0
          data(2) = 0
          val packet = new DatagramPacket(data, data.length)
          socket.receive(packet)
          mm = new ShortMessage()
          try {
            mm.setMessage((packet.getData()(0) & 0xFF).toInt, (packet.getData()(1) & 0xFF).toInt, (packet.getData()(2) & 0xFF).toInt)
            transmitter.getReceiver.send(mm, -1)
          } catch {
            case e: InvalidMidiDataException => e.printStackTrace()
          }
        } catch {
          case e1: IOException => e1.printStackTrace()
        }
      }
    }
  }
}
