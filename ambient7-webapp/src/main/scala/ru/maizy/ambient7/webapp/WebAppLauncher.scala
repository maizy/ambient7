package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import scala.util.{ Try, Failure, Success }
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener
import ru.maizy.ambient7.core.config.{ Ambient7Options, ParsingError }

object WebAppLauncher extends App {

  // TODO: how to send options to servlet context
  private var _options: Option[Ambient7Options] = None

  val eitherAppConfig = WebAppConfigReader.readAppConfig(args.toIndexedSeq)

  eitherAppConfig match {
    case Left(parsingError) =>
      webAppLogger.error(ParsingError.formatErrorsForLog(parsingError))
      Console.err.println(ParsingError.formatErrorsAndUsage(parsingError))

    case Right(opts) if opts.webAppSpecificOptions.isDefined =>
      _options = Some(opts)

      val port = opts.webAppSpecificOptions.map(_.port).getOrElse(WebAppConfigReader.DEFAULT_PORT)
      val server = new Server(port)
      val context = new WebAppContext()
      context setContextPath "/"
      context.setResourceBase("src/main/webapp")
      context.addEventListener(new ScalatraListener)
      context.addServlet(classOf[DefaultServlet], "/")

      server.setHandler(context)

      Try(server.start()) match {
        case Success(_) => server.join()
        case Failure(e) =>
          webAppLogger.error("unable to launch jetty server", e)
          server.stop()
      }

    case _ => webAppLogger.error("unknown error on launching webapp")
  }

  def options: Ambient7Options = {
    _options match {
      case None => throw new RuntimeException("options haven't loaded yet")
      case Some(opts) => opts
    }
  }
}
