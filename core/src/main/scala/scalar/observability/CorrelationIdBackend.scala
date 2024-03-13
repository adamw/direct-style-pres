package scalar.observability

import sttp.capabilities
import sttp.capabilities.Effect
import sttp.client4.{GenericRequest, Identity, Response, SyncBackend}

class CorrelationIdBackend(delegate: SyncBackend) extends SyncBackend {
  override def send[T](
      request: GenericRequest[T, Effect[Identity]]
  ): Response[T] =
    val requestWithCid = request.header(CorrelationId.HeaderName) match {
      case None    => request.header(CorrelationId.HeaderName, CorrelationId.get())
      case Some(_) => request
    }
    delegate.send(requestWithCid)
  override def close(): Unit = delegate.close()
}
