import Dependencies._

scalaVersion := "2.13.7"

scalacOptions := Seq(
  "-deprecation",
  "-explaintypes",
  "-feature",
  "-Xfatal-warnings",
  "-Ymacro-annotations",
)

name := "actor"
organization := "iammoty"
version := "1.0"

libraryDependencies ++= List(
  circe,
  sttp,
  akka,
  restito,
  scalatest,
  catsEffect,
).flatten
