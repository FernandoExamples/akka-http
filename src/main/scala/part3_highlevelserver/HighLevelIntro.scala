package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Route

object HighLevelIntro extends App {
  implicit val system = ActorSystem("HighLevelIntro")

  import system.dispatcher

  //Directives
  import akka.http.scaladsl.server.Directives._

  val simpleRoute: Route =
    path("home") {
      complete(StatusCodes.OK)
    }

  val pathGetRoute: Route =
    path("home") {
      get {
        complete(StatusCodes.OK)
      }
    }

  //chaining directives with ~

  val cahinedRoute: Route = {
    path("myEndpoint") {
      get {
        complete(StatusCodes.OK)
      } ~
        post {
          complete(StatusCodes.Forbidden)
        }
    } ~
      path("home") {
        complete(
          HttpEntity(ContentTypes.`text/html(UTF-8)`,
            """
              |<h1>Hello world</h1>
              |""".stripMargin)
        )
      }

  }

  Http().newServerAt("localhost", 8000).bind(cahinedRoute)


}
