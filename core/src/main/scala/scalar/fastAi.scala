package scalar

import org.slf4j.LoggerFactory
import ox.{supervised, useInScope}
import scalar.observability.CorrelationIdBackend
import scalar.infrastructure.{SetupNettyHttpServer, SetupOtel, SetupRedissonClient}
import sttp.client4.DefaultSyncBackend
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.loom.Id

import scala.io.StdIn

@main def fastAi(): Unit =
  val logger = LoggerFactory.getLogger(this.getClass)

  supervised {
    val scalarConfig = ScalarConfig.load()

    val otel = SetupOtel().setup()
    val redissonClient = useInScope(SetupRedissonClient.setup())(_.shutdown())
    val backend = useInScope(CorrelationIdBackend(DefaultSyncBackend()))(_.close())

    val updateCacheTasks = runUpdateCache(redissonClient)

    val apiEndpoints = List(
      fastAiServerEndpoint(
        redissonClient,
        backend,
        updateCacheTasks,
        scalarConfig,
        otel
      )
    )
    val nettyBinding = useInScope(SetupNettyHttpServer.start(otel, apiEndpoints))(_.stop())

    logger.info(s"Go to http://localhost:${nettyBinding.port}/docs to open SwaggerUI.")
    logger.info("Press ENTER key to exit.")
    val _ = StdIn.readLine()

    logger.info("Bye!")
  }
