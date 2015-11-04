package ru.maizy.ambient7

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2015
 * See LICENSE.txt for details.
 */

import com.google.common.collect.{ EvictingQueue, Queues }


object AgentApp extends App  {

  OptionParser.parse(args) match {
    case None => System.exit(2)
    case Some(opts) =>
      val writersMap = Map[Writers.Value, AppOptions => Writer](
        Writers.Console -> { new ConsoleWriter(_) },
        Writers.Interactive -> { new InteractiveWriter(_) },
        Writers.InfluxDb -> { new InfluxDbWriter(_) }
      )

      val writers = opts.writers.map(writersMap.apply).map(_(opts))
      writers.foreach(_.onInit())
      val MAX_QUEUE_SIZE = 100
      val queue = Queues.synchronizedQueue(EvictingQueue.create[Event](MAX_QUEUE_SIZE))
      val consumerThread  = new Thread {
        override def run(): Unit = {
          while(true) {
            Option(queue.poll()) foreach { event =>
              writers.foreach(_.write(event))
            }
            Thread.sleep(100)
          }
        }
      }
      consumerThread.setName("agent-consumer")
      consumerThread.start()

      // has infinity loop inside
      if (opts.useEmulator) {
        new DeviceEmulator(queue).run()
      } else {
        MT8057Service.run(queue)
      }
  }
}
