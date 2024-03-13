import com.softwaremill.SbtSoftwareMillCommon.commonSmlBuildSettings

lazy val commonSettings = commonSmlBuildSettings ++ Seq(
  organization := "com.softwaremill.scalar",
  scalaVersion := "3.3.3"
)

val tapirVersion = "1.9.11"
val sttpVersion = "4.0.0-M10+4-f5c6b550+20240312-1450-SNAPSHOT"
val scalaTest = "org.scalatest" %% "scalatest" % "3.2.18" % Test

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "scalar-direct")
  .aggregate(core)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
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
      scalaTest
    )
  )
