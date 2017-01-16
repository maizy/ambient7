package ru.maizy.ambient7.core.config.helper

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import ru.maizy.ambient7.core.config.{ Ambient7Options, ParsingError, UniversalConfigReader }

object ConfigRuleOps {

  import UniversalConfigReader._

  implicit class IfSuccessOp[T](configRes: configs.Result[T]) {

    def ifSuccess(saveValue: T => Ambient7Options): ParseResult = {
      configRes match {
        case configs.Result.Failure(error) => Left(ParsingError.withMessages(error.messages))
        case configs.Result.Success(value) => Right(saveValue(value))
      }
    }
  }
}
