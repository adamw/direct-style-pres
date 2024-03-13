package scalar.infrastructure

import scalar.cid.{CorrelationId, CorrelationIdRequestInterceptor}
import sttp.tapir.header
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.loom.{Id, NettyIdServer, NettyIdServerBinding, NettyIdServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SetupNettyHttpServer:
  def start(endpoints: List[ServerEndpoint[Any, Id]]): NettyIdServerBinding =
    val apiEndpointsWithCid = endpoints.map(
      _.endpoint.in(header[Option[String]](CorrelationId.HeaderName))
    )
    val docEndpoints: List[ServerEndpoint[Any, Id]] = SwaggerInterpreter()
      .fromEndpoints[Id](apiEndpointsWithCid, "Scalar", "1.0.0")

    val nettyOptions = NettyIdServerOptions.customiseInterceptors
      .prependInterceptor(new CorrelationIdRequestInterceptor)
      .options

    NettyIdServer(nettyOptions)
      .addEndpoints(endpoints)
      .addEndpoints(docEndpoints)
      .start()
