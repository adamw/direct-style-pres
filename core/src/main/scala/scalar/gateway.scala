package scalar

import org.slf4j.LoggerFactory
import scalar.cid.{CorrelationId, CorrelationIdRequestInterceptor}
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.loom.{Id, NettyIdServer, NettyIdServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.io.StdIn

@main def gateway(): Unit =
  val logger = LoggerFactory.getLogger(this.getClass)

  val helloEndpoint: PublicEndpoint[String, Unit, String, Any] = endpoint.get
    .in("hello")
    .in(query[String]("name"))
    .out(stringBody)

  val helloServerEndpoint: ServerEndpoint[Any, Id] = helloEndpoint.serverLogicSuccess { user =>
    logger.info(s"Saying hello to $user")
    s"Hello $user"
  }

  val apiEndpoints: List[ServerEndpoint[Any, Id]] = List(helloServerEndpoint)

  val apiEndpointsWithCid = apiEndpoints.map(_.endpoint.in(header[Option[String]](CorrelationId.HeaderName)))
  val docEndpoints: List[ServerEndpoint[Any, Id]] = SwaggerInterpreter().fromEndpoints[Id](apiEndpointsWithCid, "Scalar", "1.0.0")

  val binding = NettyIdServer(
    NettyIdServerOptions.customiseInterceptors
      .prependInterceptor(new CorrelationIdRequestInterceptor)
      .options
  ).addEndpoints(apiEndpoints)
    .addEndpoints(docEndpoints)
    .start()

  try
    logger.info(s"Go to http://localhost:${binding.port}/docs to open SwaggerUI. Press ENTER key to exit.")
    StdIn.readLine()
  finally binding.stop()
