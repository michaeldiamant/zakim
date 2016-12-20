import sbt.Keys._

val baseSettings = List(
  initialize := {
    val _ = initialize.value
    if (sys.props("java.specification.version") != "1.8")
      sys.error("Java 8 required")
  },
  organization := "mld",
  scalaVersion := "2.11.8",
  scalacOptions ++= List(
    "-language:postfixOps",
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-language:existentials",
    "-language:higherKinds",
    "-encoding", "UTF-8",
    "-unchecked",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard"),
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in(Compile, packageSrc) := true,
  javacOptions ++= List(
    "-source", "1.8", "-target", "1.8", "-Xlint", "-encoding", "UTF-8"),
  fork := true,
  mappings in(Compile, packageBin) ~= {
    _.filter(_._1.getName != "logback.xml")
  })

val testSettings = List(
  javaOptions ++= List("-Xmx2G"),
  scalacOptions in Test ++= List("-Yrangepos"),
  javaOptions in Test ++= List("-Xmx2G"),
  parallelExecution in IntegrationTest := false,
  testForkedParallel in IntegrationTest := false,
  javaOptions in IntegrationTest ++= List("-Xmx4G"))

val parser = (project in file("zakim-parser"))
  .settings(name := "zakim")
  .settings(baseSettings ++ testSettings: _*)
  .settings(libraryDependencies ++= List(
    "org.slf4j" % "slf4j-api" % "1.7.22",
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.specs2" %% "specs2-core" % "3.8.5" % "test"))

val benchmark = (project in file("zakim-benchmark"))
  .settings(name := "zakim-benchmark")
  .settings(baseSettings ++ testSettings: _*)
  .settings(libraryDependencies ++= {
    val jacksonVersion = "2.8.5"
    List(
      "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
      "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion)
  })
  .enablePlugins(JmhPlugin)
  .dependsOn(parser)

val root = (project in file("."))
  .settings(name := "aggregate")
  .settings(baseSettings ++ testSettings: _*)
  .aggregate(parser, benchmark)

