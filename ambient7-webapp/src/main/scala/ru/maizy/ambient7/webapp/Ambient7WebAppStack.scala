package ru.maizy.ambient7.webapp

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import org.scalatra.{ BadRequest, InternalServerError, NotFound, ScalatraServlet }
import ru.maizy.ambient7.webapp.servlet.error.IllegalPathParam

trait Ambient7WebAppStack extends ScalatraServlet {

  notFound {
    serveStaticResource() getOrElse "not found"
  }

  error {
    case e: java.util.NoSuchElementException => BadRequest(e.getMessage)
    case e: IllegalPathParam => NotFound(e.getMessage)
    case e: IllegalArgumentException => BadRequest(e.getMessage)
    case e: Throwable => InternalServerError(s"${e.getClass.getCanonicalName}: ${e.getMessage}")
  }
}
