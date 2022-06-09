package part2_lowlevelserver.guitarsrest

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import spray.json._
import scala.concurrent.ExecutionContext.Implicits.global


import scala.concurrent.Future
import scala.concurrent.duration.DurationInt


object Server extends App with GuitarFormatters {
  private implicit val system = ActorSystem("LowLevelRest")

  //Serialize
  val simpleGuitar = Guitar("Feder", "Startocaster", 12)
  println(simpleGuitar.toJson.prettyPrint)

  //Deserialize
  val simpleGuitarJSON =
    """
      |{
      |  "make": "Feder",
      |  "model": "Startocaster",
      |  "quantity": 3
      |}
      |""".stripMargin
  println(simpleGuitarJSON.parseJson.convertTo[Guitar])

  /**
   * SERVER
   */
  val requestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.POST, uri@Uri.Path("/api/guitars/inventory"), _, _, _) =>
      val query = uri.query()
      val guitarId = query.get("id").map(_.toInt)
      val guitarQuantity = query.get("quantity").map(_.toInt)

      val responseFuture = for {
        id <- guitarId
        quantity <- guitarQuantity
      } yield {
        GuitarController.addQuantity(id, quantity)
      }

      responseFuture.getOrElse(Future(HttpResponse(StatusCodes.BadRequest)))

    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitars/inventory"), _, _, _) =>
      val query = uri.query()
      val inStock = query.get("inStock").map(_.toBoolean)

      inStock match {
        case Some(inStock) =>
          GuitarController.getStock(inStock)
        case None => Future(HttpResponse(StatusCodes.BadRequest))
      }


    case HttpRequest(HttpMethods.GET, uri@Uri.Path("/api/guitars"), headers, entity, protocol) =>

      val query = uri.query()

      if (query.isEmpty)
        GuitarController.getAllGuitars
      else
        GuitarController.getGuitar(query)


    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitars"), headers, entity, protocol) =>
      //entities are Source[Bytes]
      val strictEntityFuture = entity.toStrict(3.seconds)
      strictEntityFuture.flatMap { strictEntity =>
        val guitarJson = strictEntity.data.utf8String
        val guitar = guitarJson.parseJson.convertTo[Guitar]
        GuitarController.saveGuitar(guitar)
      }

    case request: HttpRequest =>
      request.discardEntityBytes()
      Future {
        HttpResponse(status = StatusCodes.NotFound)
      }
  }

  Http().newServerAt("localhost", 8000).bind(requestHandler)

  /**
   * Exercise enhance the Guitar class with a quantity field, by default 0
   * - GET to /api/guitar/inventory?inStock=true which returns the guitars in stock as a JSON
   * - POST to /api/guitar/inventory?id=X&quantity=Y which adds Y guitars to hte stock for guitar with id X
   */


}
