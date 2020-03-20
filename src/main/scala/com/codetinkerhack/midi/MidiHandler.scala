package com.codetinkerhack.midi

import java.util.{HashMap, Map}
import javax.sound.midi._

import scala.beans.BeanProperty

class MidiHandler {

  @BeanProperty
  var receivers: Map[String, MidiDevice] = new HashMap[String, MidiDevice]()

  @BeanProperty
  var transmitters: Map[String, MidiDevice] = new HashMap[String, MidiDevice]()

  var device: MidiDevice = null

  val infos = MidiSystem.getMidiDeviceInfo

  for (i <- 0 until infos.length) {
    try {
      device = MidiSystem.getMidiDevice(infos(i))
      try {
        device.getReceiver
        receivers.put(infos(i).getName, device)
        println("Receiver: " + infos(i))
      } catch {
        case e: Exception =>
      }
      try {
        device.getTransmitter
        transmitters.put(infos(i).getName, device)
        println("Transmitter: " + infos(i))
      } catch {
        case e: Exception =>
      }
    } catch {
      case e: MidiUnavailableException => println(e)
    }
  }
}
