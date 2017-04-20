package ru.maizy.ambient7.analysis.notifications.action

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.collection.mutable
import ru.maizy.ambient7.analysis.notifications.event.Event

trait Action extends mutable.Subscriber[Event, mutable.Publisher[Event]]
