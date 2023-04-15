package actor

import akka.actor.ActorRef

import scala.collection.mutable

case class SearchRequestMessage(query: String, count: Int)

case class SearchResponseMessage(response: List[SearchResult])

case class SearchRequestMessageWithResult(query: String, count: Int, timeout: Int, result: mutable.Buffer[SearchResult])

case class MasterActorState(
  results: mutable.Buffer[SearchResult],
  completed: Set[ActorRef],
  requester: Option[ActorRef],
)

sealed trait SearchSystem {
  val path: String
  val name: String
}

case class Google(override val path: String) extends SearchSystem {
  override val name = "google"
}

case class Ya(override val path: String) extends SearchSystem {
  override val name = "ya"
}

case class Bing(override val path: String) extends SearchSystem {
  override val name = "bing"
}

case class SearchResult(contents: String, source: String)
