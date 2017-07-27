package com.codetinkerhack.midi.server

import java.io.{IOException, InputStream, OutputStream}
import java.net.{ServerSocket, Socket}
import javax.sound.midi.{InvalidMidiDataException, Receiver, ShortMessage, Transmitter}

import scala.beans.BeanProperty

//remove if not needed

class TCPMidiServer() extends Runnable with Transmitter {

  private val port = 6789

  private var midiSocket: ServerSocket = _

  @volatile private var keepLooping: Boolean = _

  @BeanProperty
  var receiver: Receiver = _

  class ConnectionThread(connectionSocket: Socket) extends Runnable {

    private var inputStream: InputStream = _

    private var outputStream: OutputStream = _

    try {
      inputStream = connectionSocket.getInputStream
      outputStream = connectionSocket.getOutputStream
    } catch {
      case e: IOException => e.printStackTrace()
    }

    override def run() {
      val msd = new MidiStreamDecoder(new ChecksumAckStream(inputStream, outputStream))
      var runThread = true
      val mm: ShortMessage = null
      while (runThread) {
        try {
          val msg = msd.readMidiMessage()
          receiver.send(msg, -1)
        } catch {
          case e: InvalidMidiDataException => e.printStackTrace()
          case e: java.net.SocketException => {
            runThread = false
            e.printStackTrace()
          }
          case e: IOException => {
            runThread = false
            e.printStackTrace()
          }
        }
      }
    }
  }

  override def run() {
    keepLooping = true
    var connectionSocket: Socket = null
    try {
      midiSocket = new ServerSocket(port)
      while (keepLooping) {
        println("Waiting for connection")
        try {
          connectionSocket = midiSocket.accept()
          println("Connection accepted")
          val cw = new ConnectionThread(connectionSocket)
          new Thread(cw).start()
        } catch {
          case e1: IOException => e1.printStackTrace()
        }
      }
    } catch {
      case e: IOException => e.printStackTrace()
    }
  }

  override def close() {
  }
}
