package com.codetinkerhack.midi.server

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import javax.sound.midi.InvalidMidiDataException
import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver
import javax.sound.midi.ShortMessage
import javax.sound.midi.Transmitter
import scala.beans.BeanProperty

//remove if not needed
import scala.collection.JavaConversions._

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
