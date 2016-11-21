package ru.maizy.ambient7.webapp.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite
import scalikejdbc.ConnectionPool
import ru.maizy.ambient7.webapp.AppConfig
import ru.maizy.ambient7.webapp.bootstrap.{ AppConfigInit, ScalikeJdbcInit }

trait TestsBootstrap extends ScalikeJdbcInit with AppConfigInit with LazyLogging {

  private var _connectionPool: Option[ConnectionPool] = None

  def loadTestConfig(): Unit = {
    logger.info("load test config")
    loadAppConfig()
  }

  def overwriteAppConfig(config: AppConfig): Unit = {
    _appConfig = Some(config)
  }

  def setupTestDb(): Unit = {
    _connectionPool = Some(setupDbConnectionPool(appConfig))
  }

  def migrateDb(): Unit = {
    logger.info("migrate db")
    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool.get().dataSource)
    flyway.migrate()
  }

  def destroyWebApp(): Unit = {
    if (_connectionPool.isDefined) {
      closeDbConnectionPool()
    }
  }
}

abstract class BaseServletTest
  extends ScalatraSuite
  with FlatSpecLike
  with Matchers
  with LazyLogging
  with BeforeAndAfterAll
{
  val bootstrap = new TestsBootstrap {}

  protected override def beforeAll(): Unit = {
    logger.info("before all")
    super.beforeAll()
    bootstrap.loadTestConfig()
    setupConfig(bootstrap.appConfig)
    bootstrap.setupTestDb()
    setupBootstrapBeforeTests()
    initServlets(bootstrap.appConfig)
  }

  protected override def afterAll(): Unit = {
    logger.info("after all")
    bootstrap.destroyWebApp()
    super.afterAll()
  }

  private[tests] def setupBootstrapBeforeTests(): Unit = {}

  def setupConfig(config: AppConfig): Unit = {}

  def initServlets(config: AppConfig): Unit

}
