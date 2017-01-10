package ru.maizy.ambient7.core.json

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016-2017
 * See LICENSE.txt for details.
 */

import spray.json.{ JsArray, JsValue, RootJsonFormat, pimpAny }
import ru.maizy.ambient7.core.data.{ AgentTag, AgentTags }

trait AgentTagsProtocol extends BaseProtocol
{
  implicit val agentTagFormat = jsonFormat2(AgentTag)

  implicit object AgentTagsFormat extends RootJsonFormat[AgentTags] {

    def write(tags: AgentTags): JsValue = JsArray(
        tags.tags.toVector.map(t => t.toJson)
      )

    // TODO: implements
    def read(json: JsValue): AgentTags = ???
  }
}
