package ru.maizy.ambient7.webapp.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.nio.file.Paths
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Matchers }
import org.scalatra.test.scalatest.ScalatraSuite
import scalikejdbc.ConnectionPool
import ru.maizy.ambient7.core.config.Ambient7Options
import ru.maizy.ambient7.webapp.WebAppConfigReader
import ru.maizy.ambient7.webapp.bootstrap.ScalikeJdbcInit

trait TestsBootstrap extends ScalikeJdbcInit with LazyLogging {

  private var _connectionPool: Option[ConnectionPool] = None
  private var _appOptions: Option[Ambient7Options] = None

  def appOptions: Ambient7Options = _appOptions.get

  def loadTestOptions(args: IndexedSeq[String] = IndexedSeq.empty): Unit = {
    logger.info("load test app options")
    WebAppConfigReader.readAppConfig(args) match {
      case Right(options) =>
        _appOptions = Some(options)
      case Left(error) =>
        logger.error(s"Unable to load app options: ${error.messages.mkString("; ")}")
        throw new RuntimeException("unable to load app options")
    }
    ()
  }

  def overwriteAppOptions(options: Ambient7Options): Unit = {
    _appOptions = Some(options)
    ()
  }

  def setupTestDb(): Unit = {
    _appOptions.foreach { options =>
      _connectionPool = Some(setupDbConnectionPool(options))
    }
  }

  def migrateDb(): Unit = {
    logger.info("migrate db")
    val flyway = new Flyway()
    flyway.setDataSource(ConnectionPool.get().dataSource)
    flyway.migrate()
    ()
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
    bootstrap.loadTestOptions(IndexedSeq("--config", getResourcePathString("ambient7.conf")))
    setupAppOptions(bootstrap.appOptions)
    bootstrap.setupTestDb()
    setupBootstrapBeforeTests()
    initServlets(bootstrap.appOptions)
  }

  protected override def afterAll(): Unit = {
    logger.info("after all")
    bootstrap.destroyWebApp()
    super.afterAll()
  }

  private[tests] def setupBootstrapBeforeTests(): Unit = {}

  def setupAppOptions(options: Ambient7Options): Unit = {}

  def initServlets(options: Ambient7Options): Unit

  def getResourcePathString(relPath: String): String = {
    val normPath = if (!relPath.startsWith("/")) "/" + relPath else relPath
    Paths.get(this.getClass.getResource(normPath).toURI).toString
  }

}
