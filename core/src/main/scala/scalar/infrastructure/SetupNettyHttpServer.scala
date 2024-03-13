package scalar.infrastructure

import io.opentelemetry.api.OpenTelemetry
import scalar.observability.{CorrelationId, CorrelationIdRequestInterceptor, OtelRequestInterceptor}
import sttp.tapir.header
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.netty.loom.{Id, NettyIdServer, NettyIdServerBinding, NettyIdServerOptions}
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object SetupNettyHttpServer:
  def start(otel: OpenTelemetry, endpoints: List[ServerEndpoint[Any, Id]]): NettyIdServerBinding =
    val apiEndpointsWithCid = endpoints.map(
      _.endpoint.in(header[Option[String]](CorrelationId.HeaderName))
    )
    val docEndpoints: List[ServerEndpoint[Any, Id]] = SwaggerInterpreter()
      .fromEndpoints[Id](apiEndpointsWithCid, "Scalar", "1.0.0")

    val nettyOptions = NettyIdServerOptions.customiseInterceptors
      .prependInterceptor(new CorrelationIdRequestInterceptor)
      .prependInterceptor(new OtelRequestInterceptor(otel))
      .options

    NettyIdServer(nettyOptions)
      .addEndpoints(endpoints)
      .addEndpoints(docEndpoints)
      .start()
