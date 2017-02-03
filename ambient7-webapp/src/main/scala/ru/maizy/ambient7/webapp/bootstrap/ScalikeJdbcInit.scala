package ru.maizy.ambient7.webapp.bootstrap

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import com.typesafe.scalalogging.LazyLogging
import scalikejdbc.{ ConnectionPool, ConnectionPoolSettings }
import ru.maizy.ambient7.core.config.Ambient7Options

trait ScalikeJdbcInit extends LazyLogging {

  def setupDbConnectionPool(opts: Ambient7Options): ConnectionPool = {
    logger.info("setup scalike jdbc connection pool")

    if (opts.mainDb.isEmpty || opts.mainDb.exists(_.url.isEmpty)) {
      throw new RuntimeException("main db options missing")
    }

    val dbOptions = opts.mainDb.get

    Class.forName("org.h2.Driver")

    val settings = ConnectionPoolSettings(
      initialSize = 2
    )

    // TODO: use exlicit named connection
    ConnectionPool.singleton(dbOptions.url.get, dbOptions.user, dbOptions.password, settings)
    ConnectionPool.get()
  }

  def closeDbConnectionPool(): Unit = {
    logger.info("close scalike jdbc connection pool")
    ConnectionPool.close()
  }
}
