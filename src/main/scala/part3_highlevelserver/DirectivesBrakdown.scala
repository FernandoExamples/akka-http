package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}

object DirectivesBrakdown extends App {
  implicit val system = ActorSystem("DirectivesBreakdown")

  import system.dispatcher
  import akka.http.scaladsl.server.Directives._

  /**
   * Type #1: filtering directives
   */
  val simpleHttpMethodRoute =
    post { //equivalent for get, put, patch, head, options
      complete(StatusCodes.Forbidden)
    }

  val simplePathRoute =
    path("about") {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello world</h1>"))
    }

  val complexPath =
    path("api" / "myEndpoint") {
      complete(StatusCodes.OK)
    }

  val dontConfuse =
    path("api/myEndpoint") { //esto no funciona porque el caracter / se codiifca en la URL
      complete(StatusCodes.OK)
    }

  val pathEndRoute =
    pathEndOrSingleSlash { // localhost:8000  or localhost:8000/
      complete(StatusCodes.OK)
    }

  /**
   * Type #2: Extraction directives
   */



}
