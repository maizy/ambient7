/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import org.scalatra._
import javax.servlet.ServletContext
import ru.maizy.ambient7.webapp.WebAppLauncher
import ru.maizy.ambient7.webapp.bootstrap.ScalikeJdbcInit
import ru.maizy.ambient7.webapp.servlet.{ Co2ReportServlet, DevicesServlet, RootServlet }

class ScalatraBootstrap
  extends LifeCycle
  with ScalikeJdbcInit
{

  override def init(context: ServletContext): Unit = {
    val appConfig = WebAppLauncher.options
    setupDbConnectionPool(appConfig)
    context.mount(new Co2ReportServlet(appConfig), "/co2_report")
    context.mount(new DevicesServlet(appConfig), "/devices")
    context.mount(new RootServlet, "/")
  }

  override def destroy(context: ServletContext): Unit = {
    closeDbConnectionPool()
  }
}
