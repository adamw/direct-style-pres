package scalar.observability

import io.opentelemetry.api.trace.{Span, SpanBuilder}
import io.opentelemetry.context.Context
import ox.ForkLocal

object Tracing:
  private val currentSpan = ForkLocal[Option[Span]](None)

  def withSpan[T](spanBuilder: SpanBuilder)(f: => T): T =
    currentSpan.get().foreach(parent => spanBuilder.setParent(Context.current().`with`(parent)))

    val span = spanBuilder.startSpan()
    currentSpan.scopedWhere(Some(span)) {
      try f
      catch
        case e: Exception =>
          span.recordException(e)
          throw e
      finally span.end()
    }
