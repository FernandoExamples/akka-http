package part3_highlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.server.Directives._

import scala.concurrent.duration.DurationInt
import spray.json._

import scala.util.{Failure, Success}


object HighLeveExercise extends App with DefaultJsonProtocol {
  implicit val system = ActorSystem("HighLevelExercise")

  import system.dispatcher

  /**
   * Exercise
   * - GET /api/people
   * - GET /api/people/pin
   * - GET /api/people?pint=X (same)
   * - POST /api/people with JSON payload denoting a Person, ad that person to your database
   */
  case class Person(pin: Int, name: String)

  var people = List(
    Person(1, "Alice"),
    Person(2, "Bob"),
    Person(3, "John"),
    Person(4, "Carlos"),
  )

  implicit val peopleFormatter: RootJsonFormat[Person] = jsonFormat2(Person)

  def toHttpEntity(json: String) = HttpEntity(ContentTypes.`application/json`, json)

  val route =
    pathPrefix("api" / "people") {
      get {
        (path(IntNumber) | parameter("pin".as[Int])) { pin =>
          val person = people.find(_.pin == pin)
          complete(toHttpEntity(person.toJson.prettyPrint))
        } ~
          pathEndOrSingleSlash {
            complete(toHttpEntity(people.toJson.prettyPrint))
          }
      } ~
        (post & pathEndOrSingleSlash & extractRequest & extractLog) { (request, log) =>
          val entity = request.entity
          val strictEntityFuture = entity.toStrict(2.seconds)
          val personFuture = strictEntityFuture.map(_.data.utf8String.parseJson.convertTo[Person])

          onComplete(personFuture) {
            case Success(person) =>
              log.info(s"Got person $person")
              people = person :: people
              complete(StatusCodes.OK)
            case Failure(ex) =>
              log.warning(s"Something fail with fetching the person entity")
              failWith(ex)
          }

          //          personFuture.onComplete {
          //            case Success(person) =>
          //              log.info(s"Got person $person")
          //              people = person :: people
          //            case Failure(ex) =>
          //              log.warning(s"Something fail with fetching the person entity")
          //          }
          //
          //          complete(personFuture.map(_ => StatusCodes.OK).recover {
          //            case _ => StatusCodes.InternalServerError
          //          })
        }


    }


  Http().newServerAt("localhost", 8000).bind(route)
}
