package ru.maizy.ambient7.analysis.test.notifications.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.duration.DurationInt
import ru.maizy.ambient7.analysis.notifications.data.Co2AgentData
import ru.maizy.ambient7.analysis.test.BaseSpec

class DataSpec extends BaseSpec {

  "Data.computeLimit" should "works" in {
    Co2AgentData.computeLimit(refreshRate = 5.seconds, storeDuration = 1.minute ) shouldBe 13
    Co2AgentData.computeLimit(refreshRate = 5.seconds, storeDuration = 10.seconds ) shouldBe 3
    Co2AgentData.computeLimit(refreshRate = 5.seconds, storeDuration = 5.seconds ) shouldBe 2
    Co2AgentData.computeLimit(refreshRate = 5.seconds, storeDuration = 1.seconds ) shouldBe 1
  }

}
