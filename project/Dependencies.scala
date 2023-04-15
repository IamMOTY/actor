import sbt._

object V {
  val circe = "0.14.1"
  val sttp = "3.8.11"
  val akka = "2.7.0"
  val restito = "1.1.0"
  val scalatest = "3.2.15"
  val catsEffect = "3.4.8"
}

object Dependencies {
  val circe: List[ModuleID] = List(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
  ).map(_ % V.circe)

  val sttp = List(
    "com.softwaremill.sttp.client3" %% "core" % V.sttp,
  )

  val akka = List(
    "com.typesafe.akka" %% "akka-actor" % V.akka,
  )

  val restito = List(
    "com.xebialabs.restito" % "restito" % V.restito % Test,
  )

  val scalatest = List(
    "org.scalatest" %% "scalatest" % V.scalatest % Test,
  )

  val catsEffect = List(
    "org.typelevel" %% "cats-effect" % V.catsEffect,
  )
}
