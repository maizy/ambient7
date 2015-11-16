package ru.maizy.ambient7.noiselevelagent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import javax.sound.sampled._
import java.io._

class JavaSoundRecorder {

  val wavFile: File = new File("RecordAudio.wav")
  val fileType: AudioFileFormat.Type = AudioFileFormat.Type.WAVE
  var line: TargetDataLine = _

  def getAudioFormat: AudioFormat = {
    val sampleRate = 44100
    val sampleSizeInBits = 8
    val channels = 1
    val signed = true
    val bigEndian = true
    val format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian)
    format
  }

  def start() {
    val NAME = "Built-in Input"
    try {
      val format = getAudioFormat
      val maybeInput = AudioSystem.getMixerInfo.find(_.getName == NAME)
      maybeInput match {
        case None => println("No build in input")
        case Some(input) if !AudioSystem.isLineSupported(input) => println("!isLineSupported")
        case Some(input) =>
          line = AudioSystem.getLine(input).asInstanceOf[TargetDataLine]
          line.open(format)
          line.start()
          println("Start capturing...")
          val ais = new AudioInputStream(line)
          println("Start recording...")
          AudioSystem.write(ais, fileType, wavFile)
      }

    } catch {
      case ex: LineUnavailableException => ex.printStackTrace()
      case ioe: IOException => ioe.printStackTrace()
    }
  }

  def finish() {
    line.stop()
    line.close()
    println("Finished")
  }
}


object NoiseLevelAgentApp extends App {

  def runExample(): Unit = {
    val RECORD_TIME = 2000
    val recorder = new JavaSoundRecorder()
    val stopper = new Thread(new Runnable() {
      def run() {
        try {
          Thread.sleep(RECORD_TIME)
        } catch {
          case ex: InterruptedException => ex.printStackTrace()
        }
        recorder.finish()
      }
    })
    stopper.start()
    recorder.start()
  }

  def showInfo(): Unit = {
    println(AudioSystem.getMixerInfo)
  }

  showInfo()
}
