package ru.maizy.ambient7.webapp.servlet.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2016
 * See LICENSE.txt for details.
 */

import scala.io.Codec
import org.scalatra.{ ApiFormats, RenderPipeline }
import spray.json.JsValue

trait SprayJsonSupport extends ApiFormats {

  override protected def renderPipeline: RenderPipeline = {

    val jsonRender: RenderPipeline = {
      case v: JsValue =>
        contentType = formats("json")
        response.characterEncoding = Some(Codec.UTF8.name)
        v.compactPrint
    }

    jsonRender orElse super.renderPipeline
  }
}
