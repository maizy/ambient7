package ru.maizy.ambient7.analysis.command

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.util.{ Failure, Success, Try }
import org.flywaydb.core.Flyway

object InitDbCommand {
  def run(dbUrl: String, dbUser: String, dbPassword: String): ReturnStatus = {
    val migrationManager = new Flyway
    migrationManager.setDataSource(dbUrl, dbUser, dbPassword)
    Try(migrationManager.migrate()) match {
      case Success(_) => println("Upgrading/initing DB done")
      case Failure(e) => println(s"Error while Upgrading/initing DB: $e")
    }
    ReturnStatus.success
  }
}
