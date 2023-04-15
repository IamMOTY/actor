import com.xebialabs.restito.builder.stub.StubHttp
import com.xebialabs.restito.server.StubServer
import com.xebialabs.restito.semantics.{Action, Condition}
import org.glassfish.grizzly.http.Method

object utils {
  def prepareResponse(server: StubServer, path: String, delayOpt: Option[Int]): Unit = {
    val delayAction = delayOpt match {
      case Some(d) => Action.delay(d)
      case None    => Action.noop()
    }

    val contentsAction = Action.custom { resp =>
      val count = resp.getRequest.getParameter("count").toInt
      val query = resp.getRequest.getParameter("query")

      val result = (1 to count)
        .map { i =>
          s""""$i:$query""""
        }
        .mkString("[", ", ", "]")

      resp.getWriter.write(result)
      resp
    }

    StubHttp
      .whenHttp(server)
      .`match`(Condition.method(Method.GET), Condition.startsWithUri(s"/$path"))
      .`then`(delayAction, contentsAction)
  }
}
