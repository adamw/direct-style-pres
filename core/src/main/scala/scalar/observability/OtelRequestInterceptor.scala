package scalar.observability

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.semconv.SemanticAttributes
import sttp.tapir.server.interceptor.{
  EndpointInterceptor,
  RequestHandler,
  RequestInterceptor,
  Responder
}
import sttp.tapir.server.netty.loom.Id

class OtelRequestInterceptor(otel: OpenTelemetry) extends RequestInterceptor[Id]:
  private val tracer = otel.getTracer("scalar")

  override def apply[R, B](
      responder: Responder[Id, B],
      requestHandler: EndpointInterceptor[Id] => RequestHandler[Id, R, B]
  ): RequestHandler[Id, R, B] =
    val original = requestHandler(EndpointInterceptor.noop)
    RequestHandler.from { (request, endpoints, monad) =>
      val span = tracer
        .spanBuilder("http")
        .setAttribute(SemanticAttributes.HTTP_REQUEST_METHOD, request.method.method)
        .setAttribute(SemanticAttributes.URL_PATH, request.uri.pathSegments.toString)

      Tracing.withSpan(span)(original(request, endpoints)(monad))
    }
