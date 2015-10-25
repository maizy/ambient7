package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import com.google.common.collect.{ EvictingQueue, Queues }


object AgentApp extends App  {
  val MAX_QUEUE_SIZE = 100
  val queue = Queues.synchronizedQueue(EvictingQueue.create[Event](MAX_QUEUE_SIZE))
  val consumerThread  = new Thread {
    override def run(): Unit = {
      while(true) {
        Option(queue.poll()) foreach {
          event => println(s"event: $event")
        }
        Thread.sleep(100)
      }
    }
  }
  consumerThread.setName("agent-consumer")
  consumerThread.start()

  val service = MT8057Service.run(queue)  // has infinity loop inside

}
