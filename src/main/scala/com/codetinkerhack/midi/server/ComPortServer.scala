package com.codetinkerhack.midi.server

import java.io._
import javax.sound.midi.{InvalidMidiDataException, Receiver, ShortMessage, Transmitter}

import jssc.{SerialPort, SerialPortException}

import scala.beans.BeanProperty

class ComPortServer(var portID: String,
    var baudRate: Int, 
    var dataBits: Int, 
    var stopBits: Int, 
    var parity: Int) extends Runnable with Transmitter {

  var serialPort: SerialPort = _

  var outputStream: OutputStream = _

  private var inputStream: InputStream = _

  @BeanProperty
  var receiver: Receiver = _

  var msd: MidiStreamDecoder = _

  def run() {
    val serialPort = new SerialPort(portID)
    try {
      serialPort.openPort()
      serialPort.setParams(baudRate, dataBits, stopBits, parity)
    } catch {
      case ex: SerialPortException => {
        println(ex)
        System.exit(1)
      }
    }
    var runThread = true
    val mm: ShortMessage = null
    val cpsIn = new ComPortInputStream(serialPort)
    val cpsOut = new ComPortOutputStream(serialPort)
    msd = new MidiStreamDecoder(cpsIn)
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
      } finally {
      }
    }
  }

  override def close() {
  }
}
