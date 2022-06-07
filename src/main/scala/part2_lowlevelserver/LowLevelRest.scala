package part2_lowlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
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
      guitarsFuture.map{guitars =>
        HttpResponse()

      }
  }


}
