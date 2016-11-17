package ru.maizy.ambient7.webapp.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.{ FlatSpecLike, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite
import ru.maizy.ambient7.webapp.AppConfig
import ru.maizy.ambient7.webapp.bootstrap.{ AppConfigInit, ScalikeJdbcInit }

trait TestsBootstrap extends ScalikeJdbcInit with AppConfigInit with LazyLogging {

  def init(): Unit = {
    logger.info("load test config")
    val config = loadAppConfig()
    setupDbConnectionPool(config)
  }

  def destroy(): Unit = {
    closeDbConnectionPool()
  }
}

abstract class BaseServletTest
  extends ScalatraSuite
  with FlatSpecLike
  with Matchers
  with LazyLogging
{
  protected override def afterAll(): Unit = {
    super.afterAll()
    bootstrap.destroy()
  }

  def setupConfig(config: AppConfig): Unit = {}

  def initServlets(config: AppConfig): Unit

  val bootstrap = new TestsBootstrap {}
  bootstrap.init()
  setupConfig(bootstrap.appConfig)
  initServlets(bootstrap.appConfig)

}
