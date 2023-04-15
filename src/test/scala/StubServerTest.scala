import actor._
import akka.actor.{ActorSystem, Props}
import com.xebialabs.restito.server.StubServer
import org.scalatest._
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.mutable

class StubServerTest extends AnyFunSuite with BeforeAndAfter {
  private var server: StubServer = _

  before {
    server = new StubServer(8000)
    server.start()
  }

  after {
    server.stop()
  }

  private val allSearchSystems = List(
    Google("google.com"),
    Ya("ya.ru"),
    Bing("bing.com"),
  )

  def baseTest(
    testName: String,
    searchSystems: List[(SearchSystem, Option[Int])],
    count: Int,
    query: String,
    timeout: Int,
    expected: List[SearchResult],
  ): Unit = {
    test(testName) {
      searchSystems.foreach { case (ss, tl) =>
        utils.prepareResponse(server, ss.path, tl)
      }

      val system = ActorSystem("TestSystem")
      val props = Props(classOf[MasterActor], allSearchSystems)
      val masterActor = system.actorOf(props, "master")

      val msg = SearchRequestMessageWithResult(query, count, timeout, mutable.Buffer())
      masterActor ! msg

      Thread.sleep(timeout)

      val actual = msg.result
      assert(expected.size == actual.size && expected.toSet == actual.toSet)
    }
  }

  def mkExpected(searchSystems: List[SearchSystem], query: String, count: Int): List[SearchResult] =
    searchSystems.flatMap { ss =>
      (1 to count).map { i =>
        val res = s"$i:$query"
        SearchResult(res, ss.name)
      }
    }

  baseTest(
    testName = "All systems",
    searchSystems = allSearchSystems.map { (_, None) },
    count = 5,
    query = "query",
    timeout = 3000,
    expected = mkExpected(allSearchSystems, "query", 5),
  )

  baseTest(
    testName = "No systems",
    searchSystems = List(),
    count = 100,
    query = "query",
    timeout = 3000,
    expected = List(),
  )

  baseTest(
    testName = "All requests exceed time out",
    searchSystems = allSearchSystems.map { (_, Some(3000)) },
    count = 100,
    query = "query",
    timeout = 2000,
    expected = List(),
  )

  baseTest(
    testName = "ya exceeds time out",
    searchSystems = allSearchSystems.map {
      case ss @ Ya(_) => (ss, Some(10000))
      case ss           => (ss, None)
    },
    count = 5,
    query = "query",
    timeout = 3000,
    expected = mkExpected(
      allSearchSystems.filter {
        case Ya(_) => false
        case _       => true
      },
      "query",
      5,
    ),
  )

}
