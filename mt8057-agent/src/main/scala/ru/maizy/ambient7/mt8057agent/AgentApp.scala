package ru.maizy.ambient7.mt8057agent

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.{ LoggerContext, Logger => LogbackLogger, Level }
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.{ OutputStreamAppender, ConsoleAppender, FileAppender }
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger
import com.google.common.collect.{ EvictingQueue, Queues }


object AgentApp extends App {

  val logger = Logger(LoggerFactory.getLogger("ru.maizy.ambient7.mt8057agent"))

  def setupLogging(opts: AppOptions): Unit = {
    val logContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

    def setupAppender(appender: OutputStreamAppender[ILoggingEvent], pattern: String, normalLevel: String): Unit = {
      val encoder = new PatternLayoutEncoder
      encoder.setPattern(pattern)
      encoder.setContext(logContext)
      encoder.start()

      if (!opts.verboseLogging) {
        val filter = new ThresholdFilter
        filter.setLevel(normalLevel)
        filter.start()
        appender.addFilter(filter)
      }
      appender.setEncoder(encoder)
      appender.setContext(logContext)
      appender.start()
      rootLogger.addAppender(appender)
    }

    val consoleAppender = new ConsoleAppender[ILoggingEvent]
    consoleAppender.setTarget("System.err")
    setupAppender(
      consoleAppender,
      pattern = "%gray(%d{HH:mm:ss.SSS}) %highlight(%.-1level) [%cyan(%logger{5})]: %msg%n",
      normalLevel = "WARN"
    )

    opts.logFile.foreach { file =>
      val fileAppender = new FileAppender[ILoggingEvent]
      fileAppender.setAppend(true)
      fileAppender.setFile(file.toString)
      setupAppender(
        fileAppender,
        pattern = "%d %level [%logger]: %msg%n",
        normalLevel = "INFO"
      )
    }

    if (!opts.verboseLogging) {
      rootLogger.setLevel(Level.INFO)
    }
  }

  OptionParser.parse(args) match {
    case None => {
      logger.error("Wrong app options, exiting")
      System.exit(2)
    }
    case Some(opts) =>
      setupLogging(opts)

      val writersMap = Map[Writers.Value, AppOptions => Writer](
        Writers.Console -> { new ConsoleWriter(_) },
        Writers.Interactive -> { new InteractiveWriter(_) },
        Writers.InfluxDb -> { new InfluxDbWriter(_) }
      )

      logger.info("Start agent with writers: " + opts.writers.mkString(", "))

      val writers = opts.writers.map(writersMap.apply).map(_(opts))
      writers.foreach(_.onInit())
      val MAX_QUEUE_SIZE = 100
      val queue = Queues.synchronizedQueue(EvictingQueue.create[Event](MAX_QUEUE_SIZE))
      val consumerThread  = new Thread {
        override def run(): Unit = {
          while(true) {
            Option(queue.poll()) foreach { event =>
              writers.foreach(_.write(event))
            }
            Thread.sleep(100)
          }
        }
      }
      consumerThread.setName("agent-consumer")
      consumerThread.start()

      // has infinity loop inside
      if (opts.useEmulator) {
        logger.info("Use emulator mode")
        new DeviceEmulator(queue).run()
      } else {
        MT8057Service.run(queue)
      }
  }
}
