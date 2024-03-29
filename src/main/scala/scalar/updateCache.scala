package scalar

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import ox.channels.{Channel, Sink}
import ox.syntax.forever
import ox.{fork, Ox}

import scala.util.control.NonFatal

case class UpdateCacheTask(q: Question, a: Answer)

def runUpdateCache(client: RedissonClient)(using Ox): Sink[UpdateCacheTask] =
  val logger = LoggerFactory.getLogger(classOf[UpdateCacheTask])
  val updateCacheTasks = Channel.bufferedDefault[UpdateCacheTask]
  fork {
    forever {
      val task = updateCacheTasks.receive()
      try
        logger.debug(s"Storing answer for \"${task.q.question}\" in the cache")
        client.getBucket[String](task.q.hash).set(task.a.answer)
      catch case NonFatal(e) => logger.error(s"Cannot set ${task.q} to ${task.a}", e)
    }
  }
  updateCacheTasks
