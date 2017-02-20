package ru.maizy.ambient7.core.tests.data

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.data.{ AgentTag, AgentTags }
import ru.maizy.ambient7.core.tests.BaseSpec

class AgentTagsSpec extends BaseSpec {

  "AgentTags" should "encode & decode tags and tags values with escaping" in {
    val originalString = "atag=va\\,lue,b\\,tag=value"
    AgentTags(originalString) shouldBe
      AgentTags(Seq(AgentTag("atag", "va,lue"), AgentTag("b,tag", "value")))

    AgentTags(originalString).encoded shouldBe originalString
  }
}
