package ru.maizy.ambient7.mt8057agent.tests

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import scala.util.Try
import scalaj.http.{ HttpResponse, HttpRequest }
import ru.maizy.ambient7.mt8057agent._

class InfluxDbWriterSpec extends AbstractBaseSpec with WritersTestUtils {

  class StubbedInfluxdbWriter(opts: AppOptions) extends InfluxDbWriter(opts) {

    override protected def performRequest(request: HttpRequest): Try[HttpResponse[String]] =
      super.performRequest(request)

    def lastRequest(): HttpRequest = ???
  }

  val w = new InfluxDbWriter(AppOptions())

  "InfluxDbWriter" should "..." in {
  }


}
