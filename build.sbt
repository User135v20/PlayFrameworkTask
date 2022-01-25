name := """play-scala-seed"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
routesGenerator := InjectedRoutesGenerator
scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

val circeVersion = "0.14.1"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.3.3"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
libraryDependencies +=  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"

javaOptions ++= Seq("-Xmx4g", "-Xms4g", "-XX:MaxPermSize=4096M")