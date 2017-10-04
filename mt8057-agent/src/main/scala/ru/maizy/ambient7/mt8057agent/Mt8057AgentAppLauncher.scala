package ru.maizy.ambient7.mt8057agent

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{ Level, LoggerContext, Logger => LogbackLogger }
import ch.qos.logback.core.{ ConsoleAppender, FileAppender, OutputStreamAppender }
import com.google.common.collect.{ EvictingQueue, Queues }
import org.slf4j.LoggerFactory
import ru.maizy.ambient7.core.config.options.Writers
import ru.maizy.ambient7.core.config.{ Ambient7Options, ParsingError }

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

object Mt8057AgentAppLauncher extends App {

  Mt8057ConfigReader.readAppConfig(args.toIndexedSeq) match {
    case Left(parsingError) =>
      agentLogger.error(ParsingError.formatErrorsForLog(parsingError))
      Console.err.println(ParsingError.formatErrorsAndUsage(parsingError))

    case Right(opts) if opts.mt8057AgentSpecificOptions.isEmpty =>
      agentLogger.error("unknown error")

    case Right(opts) =>
      setupLogging(opts)

      val writersMap = Map[Writers.Value, Ambient7Options => Writer](
        Writers.Console -> { new ConsoleWriter(_) },
        Writers.Interactive -> { new InteractiveWriter(_) },
        Writers.InfluxDb -> { new InfluxDbWriter(_) }
      )

      agentLogger.info(
        "Start agent with writers: " +
          opts.mt8057AgentSpecificOptions.map(_.writers.mkString(", ")).getOrElse("")
      )

      val writers = opts.mt8057AgentSpecificOptions
        .map(_.writers.map(writersMap.apply).map(_(opts)))
        .getOrElse(Set.empty)

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
      if (opts.mt8057AgentSpecificOptions.exists(_.useEmulator)) {
        agentLogger.info("Use emulator mode")
        new DeviceEmulator(queue).run()
      } else {
        MT8057Service.run(queue)
      }
  }

  def setupLogging(opts: Ambient7Options): Unit = {
    val logContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[LogbackLogger]

    val verboseLogging = opts.mt8057AgentSpecificOptions.exists(_.verboseLogging)

    def setupAppender(appender: OutputStreamAppender[ILoggingEvent], pattern: String, normalLevel: String): Unit = {
      val encoder = new PatternLayoutEncoder
      encoder.setPattern(pattern)
      encoder.setContext(logContext)
      encoder.start()

      if (!verboseLogging) {
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

    opts.mt8057AgentSpecificOptions.flatMap(_.logFile).foreach { file =>
      val fileAppender = new FileAppender[ILoggingEvent]
      fileAppender.setAppend(true)
      fileAppender.setFile(file.toString)
      setupAppender(
        fileAppender,
        pattern = "%d %level [%logger]: %msg%n",
        normalLevel = "INFO"
      )
    }

    if (!verboseLogging) {
      rootLogger.setLevel(Level.INFO)
    }
  }
}
