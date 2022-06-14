package part3_highlevelserver

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import part2_lowlevelserver.guitarsrest.GuitarDB._
import part2_lowlevelserver.guitarsrest.{Guitar, GuitarDB, GuitarFormatters}
import spray.json._

import scala.concurrent.duration.DurationInt

object HighLevelExample extends App with GuitarFormatters {
  implicit val system = ActorSystem("HighLevelExample")

  import system.dispatcher

  private val guitarDB = system.actorOf(Props[GuitarDB], "guitarDb")
  private val guitarList = List(
    Guitar("Feder", "stratocaster"),
    Guitar("Gibson", "Les Paul"),
    Guitar("Martin", "LX1")
  )

  guitarList.foreach { guitar =>
    guitarDB ! CreateGuitar(guitar)
  }

  implicit val timeout = Timeout(2.seconds)
  val guitarServerRoute =
    path("api" / "guitar") {
      parameter("id".as[Int]) { guitarId: Int =>
        get {
          val guitarFuture = (guitarDB ? FindGuitarById(guitarId)).mapTo[Option[Guitar]]
          val entityFuture = guitarFuture.map { guitarOption =>
            HttpEntity(ContentTypes.`application/json`, guitarOption.toJson.prettyPrint)
          }
          complete(entityFuture)
        }
      } ~
        get {
          val guitarsFuture = (guitarDB ? FindAllGuitars).mapTo[List[Guitar]]
          val entityFuture = guitarsFuture.map { guitars =>
            HttpEntity(ContentTypes.`application/json`, guitars.toJson.prettyPrint)
          }
          complete(entityFuture)
        }
    } ~
      path("api" / "guitar" / IntNumber) { guitarId =>
        get {
          val guitarFuture = (guitarDB ? FindGuitarById(guitarId)).mapTo[Option[Guitar]]
          val entityFuture = guitarFuture.map { guitarOption =>
            HttpEntity(ContentTypes.`application/json`, guitarOption.toJson.prettyPrint)
          }
          complete(entityFuture)
        }

      } ~
      path("api" / "guitar" / "inventory") {
        get {
          parameter("inStock".as[Boolean]) { inStock =>
            val guitarFuture = (guitarDB ? FindGuitarsInStock(inStock)).mapTo[List[Guitar]]
            val entityFuture = guitarFuture.map { guitarList =>
              HttpEntity(ContentTypes.`application/json`, guitarList.toJson.prettyPrint)
            }
            complete(entityFuture)

          }
        }

      }

  def toHttpEntity(payload: String) = HttpEntity(ContentTypes.`application/json`, payload)

  val simplifiedGuitarRoute = {
    (pathPrefix("api" / "guitar") & get) {
      path("inventory") {
        parameter("inStock".as[Boolean]) { inStock =>
          val entityFuture = (guitarDB ? FindGuitarsInStock(inStock)).mapTo[List[Guitar]].map(_.toJson.prettyPrint).map(toHttpEntity)
          complete(entityFuture)
        }
      } ~
        (path(IntNumber) | parameter("id".as[Int])) { guitarId =>
          val entityFuture = (guitarDB ? FindGuitarById(guitarId)).mapTo[Option[Guitar]].map(_.toJson.prettyPrint).map(toHttpEntity)
          complete(entityFuture)
        } ~
        pathEndOrSingleSlash {
          val entityFuture = (guitarDB ? FindAllGuitars).mapTo[List[Guitar]].map(_.toJson.prettyPrint).map(toHttpEntity)
          complete(entityFuture)
        }
    }

    Http().newServerAt("localhost", 8000).bind(guitarServerRoute)
  }

}

