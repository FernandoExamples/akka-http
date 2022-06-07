ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"
val AkkaVersion = "2.6.19"
val AkkaHttpVersion = "10.2.9"

lazy val root = (project in file("."))
  .settings(
    name := "akka-http"
  )

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.2.12" % Test,
)
