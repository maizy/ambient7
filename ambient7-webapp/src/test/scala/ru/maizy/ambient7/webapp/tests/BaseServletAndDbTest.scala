package ru.maizy.ambient7.webapp.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import java.util.regex.Pattern
import scala.io.Source
import com.typesafe.scalalogging.LazyLogging
import scalikejdbc._

abstract class BaseServletAndDbTest
  extends BaseServletTest
  with LazyLogging
{
  override private[tests] def setupBootstrapBeforeTests(): Unit = {
    super.setupBootstrapBeforeTests()
    bootstrap.migrateDb()
  }

  def loadSql(file: String, substitutions: Map[String, String] = Map.empty): Unit = {
    val content = Source
      .fromInputStream(getClass.getClassLoader.getResourceAsStream(s"sql/$file"))
      .mkString

    DB localTx { implicit session =>
      content
        // very naive experession spliting, but should works for test data
        .split(";")
        .filterNot(_.trim == "")
        .map { exp =>
          substitutions.foldLeft[String](exp) { (acc, p) =>
            acc.replaceAll(Pattern.quote(s"%${p._1}%"), p._2)
          }
        }
        .foreach { exp =>
          logger.info(s"Execute SQL expression:\n$exp")
          val affectedRows = SQL(exp).update.apply()
          logger.info(s"Done. Affected rows: $affectedRows")
        }
    }
  }


}
