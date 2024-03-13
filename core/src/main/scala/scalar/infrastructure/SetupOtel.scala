package scalar.infrastructure

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.`export`.BatchSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes
import ox.{Ox, useInScope}

import java.util.concurrent.TimeUnit

class SetupOtel:
  def setup()(using Ox): OpenTelemetry =
    val jaegerOtlpExporter = OtlpGrpcSpanExporter.builder
      .setEndpoint("http://localhost:4317")
      .setTimeout(30, TimeUnit.SECONDS)
      .build

    val serviceNameResource =
      Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "fast-ai"))

    val tracerProvider = useInScope(
      SdkTracerProvider.builder
        .addSpanProcessor(BatchSpanProcessor.builder(jaegerOtlpExporter).build)
        .setResource(Resource.getDefault.merge(serviceNameResource))
        .build()
    )(_.close())
    val openTelemetry = OpenTelemetrySdk.builder.setTracerProvider(tracerProvider).build

    openTelemetry
