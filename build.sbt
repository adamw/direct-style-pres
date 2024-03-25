import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

val tapirVersion = "1.9.11"
val sttpVersion = "4.0.0-M11"
val otelVersion = "1.36.0"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18" % Test

commonSmlBuildSettings

organization := "com.softwaremill.scalar"

scalaVersion := "3.3.3"

name := "scalar-direct"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.tapir" %% "tapir-netty-server-loom" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
  "com.softwaremill.sttp.tapir" %% "tapir-json-pickler" % tapirVersion,
  "com.softwaremill.sttp.client4" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client4" %% "upickle" % sttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.5.3",
  "com.softwaremill.ox" %% "core" % "0.0.23",
  "org.redisson" % "redisson" % "3.27.2",
  "com.github.pureconfig" %% "pureconfig-core" % "0.17.6",
  "io.opentelemetry" % "opentelemetry-api" % otelVersion,
  "io.opentelemetry" % "opentelemetry-sdk" % otelVersion,
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % otelVersion,
  "io.opentelemetry.semconv" % "opentelemetry-semconv" % "1.23.1-alpha",
  scalaTest
)
