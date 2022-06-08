package part2_lowlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.pattern.ask
import akka.util.Timeout
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

case class Guitar(make: String, model: String)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)

  case class GuitarCreated(id: Int)

  case class FindGuitarById(id: Int)

  case object FindAllGuitars
}

class GuitarDB extends Actor with ActorLogging {

  import GuitarDB._

  private var guitars: Map[Int, Guitar] = Map()
  private var currentGuitarId = 0

  override def receive: Receive = {
    case FindAllGuitars =>
      log.info("Searching for all guitars")
      sender() ! guitars.values.toList

    case FindGuitarById(id) =>
      log.info("Searching guitar by id")
      sender() ! guitars.get(id)

    case CreateGuitar(guitar) =>
      log.info(s"Adding $guitar with $currentGuitarId")
      guitars = guitars + (currentGuitarId -> guitar)
      sender() ! GuitarCreated(currentGuitarId)
      currentGuitarId += 1

  }
}

trait GuitarStoreJsonProtocol extends DefaultJsonProtocol {
  implicit val guitarFormat = jsonFormat2(Guitar)
}


object LowLevelRest extends App with GuitarStoreJsonProtocol {
  implicit val system = ActorSystem("LowLevelRest")

  import scala.concurrent.ExecutionContext.Implicits.global
  import GuitarDB._

  //Serialize
  val simpleGuitar = Guitar("Feder", "Startocaster")
  println(simpleGuitar.toJson.prettyPrint)

  //Deserialize
  val simpleGuitarJSON =
    """
      |{
      |  "make": "Feder",
      |  "model": "Startocaster"
      |}
      |""".stripMargin
  println(simpleGuitarJSON.parseJson.convertTo[Guitar])

  /**
   * Setup
   */

  val guitarDB = system.actorOf(Props[GuitarDB], "guitarDb")
  val guitarList = List(
    Guitar("Feder", "stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach { guitar =>
    guitarDB ! CreateGuitar(guitar)
  }

  /**
   * Server Code
   */

  implicit val timeout = Timeout(2.seconds)

  val requestHandler: HttpRequest => Future[HttpResponse] = {

    case HttpRequest(HttpMethods.GET, Uri.Path("/api/guitars"), headers, entity, protocol) =>
      val guitarsFuture = (guitarDB ? FindAllGuitars).mapTo[List[Guitar]]
      guitarsFuture.map { guitars =>
        HttpResponse(entity = HttpEntity(ContentTypes.`application/json`, guitars.toJson.prettyPrint))
      }

    case HttpRequest(HttpMethods.POST, Uri.Path("/api/guitars"), headers, entity, protocol) =>
      //entities are Source[Bytes]
      val strictEntityFuture = entity.toStrict(3.seconds)
      strictEntityFuture.flatMap { strictEntity =>
        val guitarJson = strictEntity.data.utf8String
        val guitar = guitarJson.parseJson.convertTo[Guitar]

        val guitarCreatedFuture = (guitarDB ? CreateGuitar(guitar)).mapTo[GuitarCreated]

        guitarCreatedFuture.map { guitarCreated =>
          HttpResponse(StatusCodes.OK)

        }

      }


    case request: HttpRequest =>
      request.discardEntityBytes()
      Future {
        HttpResponse(status = StatusCodes.NotFound)
      }


  }

  Http().newServerAt("localhost", 8000).bind(requestHandler)


}
