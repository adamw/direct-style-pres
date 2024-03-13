package scalar.observability

import sttp.tapir.server.interceptor.{
  EndpointInterceptor,
  RequestHandler,
  RequestInterceptor,
  Responder
}
import sttp.tapir.server.netty.loom.Id

class CorrelationIdRequestInterceptor extends RequestInterceptor[Id]:
  override def apply[R, B](
      responder: Responder[Id, B],
      requestHandler: EndpointInterceptor[Id] => RequestHandler[Id, R, B]
  ): RequestHandler[Id, R, B] =
    val original = requestHandler(EndpointInterceptor.noop)
    RequestHandler.from { (request, endpoints, monad) =>
      CorrelationId.withCid(request.header(CorrelationId.HeaderName)) {
        original(request, endpoints)(monad)
      }
    }
