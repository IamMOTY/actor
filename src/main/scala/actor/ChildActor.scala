package actor

import akka.actor.Actor
import io.circe.Decoder
import io.circe.parser.decode
import sttp.client3._

case class ChildActor(searchSystem: SearchSystem) extends Actor {
  def receive: Receive = { case SearchRequestMessage(query, count) =>
    val res = getTopResults(searchSystem, query, count)
    sender().tell(SearchResponseMessage(res), self)
    context.stop(self)
  }

  def getTopResults(searchSystem: SearchSystem, query: String, top: Int): List[SearchResult] = {
    val request = basicRequest.get(uri"http://localhost:8000/${searchSystem.path}?query=$query&count=$top")
    val response = request.send(HttpURLConnectionBackend())

    val result = response.body
      .flatMap { body =>
        val contents = decode[Contents](body)
        val results = contents.map { _.value.map(SearchResult(_, searchSystem.name)) }

        results
      }
      .getOrElse(List())
    result
  }

  case class Contents(value: List[String])
  object Contents {
    implicit val decoder: Decoder[Contents] =
      Decoder[List[String]].map(Contents(_))
  }
}
