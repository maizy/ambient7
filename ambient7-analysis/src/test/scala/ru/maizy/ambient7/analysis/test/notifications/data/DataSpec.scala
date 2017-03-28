package ru.maizy.ambient7.analysis.test.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.DurationInt
import ru.maizy.ambient7.analysis.notifications.data.Data
import ru.maizy.ambient7.analysis.test.BaseSpec

class DataSpec extends BaseSpec {

  "Data.computeLimit" should "works" in {
    Data.computeLimit(refreshRate = 5.seconds, storeDuration = 1.minute ) shouldBe 13
    Data.computeLimit(refreshRate = 5.seconds, storeDuration = 10.seconds ) shouldBe 3
    Data.computeLimit(refreshRate = 5.seconds, storeDuration = 5.seconds ) shouldBe 2
    Data.computeLimit(refreshRate = 5.seconds, storeDuration = 1.seconds ) shouldBe 1
  }

}
