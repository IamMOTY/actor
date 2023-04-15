package actor

import akka.actor.{Actor, Props}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.mutable

class MasterActor(searchSystems: List[SearchSystem]) extends Actor {

  def receive: Receive = receiveWithState(MasterActorState(mutable.Buffer.empty, Set.empty, None))

  def receiveWithState: MasterActorState => Receive = state => {
    case SearchRequestMessageWithResult(query, count, timeout, result) =>
      context.become(
        receiveWithState(
          state.copy(
            requester = Some(sender()),
            results = result,
          ),
        ),
      )

      searchSystems.zipWithIndex.foreach { case (ss, i) =>
        val child = context.actorOf(Props(classOf[ChildActor], ss), i.toString)
        child.tell(SearchRequestMessage(query, count), self)
      }

      Future {
        Thread.sleep(timeout)
        respondWithSearchResult(state)
        context.stop(self)
      }

    case SearchResponseMessage(res) =>
      state.results ++= res

      context.become(
        receiveWithState(
          state.copy(
            completed = state.completed + sender(),
          ),
        ),
      )

      if (state.completed.size == searchSystems.size) {
        respondWithSearchResult(state)
      }
  }

  def respondWithSearchResult(state: MasterActorState): Unit = {
    state.requester match {
      case Some(r) => r.tell(SearchResponseMessage(state.results.toList), self)
      case None    => throw new IllegalStateException("Receiver is None")
    }
  }

}
