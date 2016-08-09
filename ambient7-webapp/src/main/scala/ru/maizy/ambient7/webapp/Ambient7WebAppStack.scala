package ru.maizy.ambient7.webapp

import org.scalatra.ScalatraServlet

trait Ambient7WebAppStack extends ScalatraServlet {

  notFound {
    serveStaticResource() getOrElse "not found"
  }

}
