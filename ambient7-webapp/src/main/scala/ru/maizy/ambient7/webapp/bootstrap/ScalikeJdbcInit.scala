package ru.maizy.ambient7.webapp.bootstrap

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{ ConnectionPool, ConnectionPoolSettings }
import ru.maizy.ambient7.webapp.AppConfig

trait ScalikeJdbcInit extends LazyLogging {

  def setupDbConnectionPool(config: AppConfig): ConnectionPool = {
    logger.info("setup scalike jdbc connection pool")

    Class.forName("org.h2.Driver")

    val settings = ConnectionPoolSettings(
      initialSize = 2
    )

    // TODO: use exlicit named connection
    ConnectionPool.singleton(config.dbUrl, config.dbUser, config.dbPassword, settings)
    ConnectionPool.get()
  }

  def closeDbConnectionPool(): Unit = {
    logger.info("close scalike jdbc connection pool")
    ConnectionPool.close()
  }
}
