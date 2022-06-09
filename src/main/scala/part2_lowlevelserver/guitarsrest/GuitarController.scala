package part2_lowlevelserver.guitarsrest

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.Uri.Query
import akka.pattern.ask
import akka.util.Timeout
import spray.json.enrichAny
import part2_lowlevelserver.guitarsrest.GuitarDB._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.Implicits.global


object GuitarController extends GuitarFormatters {

  private implicit val system: ActorSystem = ActorSystem("LowLevelRest")
  private implicit val timeout: Timeout = Timeout(2.seconds)


  private val guitarDB = system.actorOf(Props[GuitarDB], "guitarDb")
  private val guitarList = List(
    Guitar("Feder", "stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach { guitar =>
    guitarDB ! CreateGuitar(guitar)
  }

  def getGuitar(query: Query): Future[HttpResponse] = {
    val guitarId = query.get("id").map(_.toInt)
    guitarId match {
      case None => Future(HttpResponse(StatusCodes.NotFound))
      case Some(id: Int) =>
        val guitarFuture = (guitarDB ? FindGuitarById(id)).mapTo[Option[Guitar]]
        guitarFuture.map {
          case None => HttpResponse(StatusCodes.NotFound)
          case Some(guitar) => HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, guitar.toJson.compactPrint))
        }
    }
  }

  def getAllGuitars: Future[HttpResponse] = {
    val guitarsFuture = (guitarDB ? FindAllGuitars).mapTo[List[Guitar]]
    guitarsFuture.map { guitars =>
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, guitars.toJson.prettyPrint))
    }
  }

  def saveGuitar(guitar: Guitar): Future[HttpResponse] = {
    val guitarCreatedFuture = (guitarDB ? CreateGuitar(guitar)).mapTo[GuitarCreated]

    guitarCreatedFuture.map { guitarCreated =>
      HttpResponse(StatusCodes.OK)

    }
  }

  def addQuantity(id: Int, quantity: Int): Future[HttpResponse] = {
    val newGuitarFuture = (guitarDB ? AddQuantity(id, quantity)).mapTo[Option[Guitar]]
    newGuitarFuture.map(_ => HttpResponse(StatusCodes.OK))
  }

  def getStock(inStock: Boolean): Future[HttpResponse] = {
    val guitarsFuture = (guitarDB ? FindGuitarsInStock(inStock)).mapTo[List[Guitar]]

    guitarsFuture.map { list =>
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, list.toJson.prettyPrint))
    }

  }
}
