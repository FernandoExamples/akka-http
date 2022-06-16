ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"

lazy val root = (project in file("."))
  .settings(
    name := "akka-http"
  )

libraryDependencies ++= Seq(
  // akka actors
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  //akka http
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  //Test
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.11"
)
