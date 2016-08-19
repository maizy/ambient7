package ru.maizy.ambient7.webapp

import org.scalatra.{ BadRequest, InternalServerError, ScalatraServlet }

trait Ambient7WebAppStack extends ScalatraServlet {

  notFound {
    serveStaticResource() getOrElse "not found"
  }

  error {
    case e: java.util.NoSuchElementException => BadRequest(e.getMessage)
    case e: IllegalArgumentException => BadRequest(e.getMessage)
    case e: Throwable => InternalServerError(s"${e.getClass.getCanonicalName}: ${e.getMessage}")
  }
}
