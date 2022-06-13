package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
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
  val pathExtractionRoute =
    path("api" / "item" / IntNumber) { (item: Int) =>
      println(s"I've got a number in my path ${item}")
      complete(StatusCodes.OK)
    }

  //  Http().newServerAt("localhost", 8000).bind(pathExtractionRoute)

  val pathMultiExtractionRoute =
    path("api" / "item" / IntNumber / IntNumber) { (item: Int, id: Int) =>
      println(s"I've got two numbers in my path ${item} - $id")
      complete(StatusCodes.OK)
    }

  val queryParamExtractionRoute =
    path("api" / "item") {
      parameter('id.as[Int]) { (itemId: Int) =>
        println(s"I've extracted the ID ${itemId}")
        complete(StatusCodes.OK)
      }
    }

  val extractRequestRoute =
    path("controlEndpoint") {
      extractRequest { (request: HttpRequest) =>
        println(s"I've got http request: ${request}")
        complete(StatusCodes.OK)

      }
    }

}
