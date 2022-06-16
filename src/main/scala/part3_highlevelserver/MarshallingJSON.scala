package part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration.DurationInt
import spray.json._

import scala.util.Success


case class Player(nickname: String, characterClass: String, level: Int)

object GameAreaMap {
  sealed trait GameCommand

  case object GetAllPlayers extends GameCommand

  case class GetPlayer(nickname: String) extends GameCommand

  case class GetPlayersByClass(characterClass: String) extends GameCommand

  case class AddPlayer(player: Player) extends GameCommand

  case class RemovePlayer(player: Player) extends GameCommand

  case object OperationSuccess extends GameCommand
}


class GameAreaMap extends Actor with ActorLogging {

  import GameAreaMap._

  var players = Map[String, Player]()

  override def receive: Receive = {
    case GetAllPlayers =>
      log.info("Getting all players")
      sender() ! players.values.toList

    case GetPlayer(nickname) =>
      log.info(s"Getting player with nickname $nickname")
      sender() ! players.get(nickname)

    case GetPlayersByClass(characterClass) =>
      log.info(s"Getting All players with the character $characterClass")
      sender() ! players.values.toList.filter(_.characterClass == characterClass)

    case AddPlayer(player) =>
      log.info(s"Trying to add player $player")
      players = players + (player.nickname -> player)
      sender() ! OperationSuccess

    case RemovePlayer(player) =>
      log.info(s"Trying to remove $player")
      players = players - player.nickname
      sender() ! OperationSuccess
  }
}

trait PlayerJsonProtocol extends DefaultJsonProtocol {
  implicit val playerFormat = jsonFormat3(Player)
}


object MarshallingJSON extends App with PlayerJsonProtocol with SprayJsonSupport {
  implicit val system = ActorSystem("MarshallingJSON")

  import GameAreaMap._
  import system.dispatcher

  val gameActor = system.actorOf(Props[GameAreaMap], "rockTheJVMAreaMap")
  val playerList = List(
    Player("martin", "Warrior", 70),
    Player("roland", "Eld", 67),
    Player("fer", "Wizard", 90),
  )

  playerList.foreach { player =>
    gameActor ! AddPlayer(player)
  }


  /**
   *
   */
  implicit val timeout = Timeout(2.seconds)


  val gameRoute =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { characterClass =>
          val playersByClass = (gameActor ? GetPlayersByClass(characterClass)).mapTo[List[Player]]
          complete(playersByClass) //se esta haciendo la conversion automatica por el trait SpraySupport
        } ~
          (path(Segment) | parameter("nickname")) { nickname =>
            val playerOption = (gameActor ? GetPlayer(nickname)).mapTo[Option[Player]]
            complete(playerOption)
          } ~
          pathEndOrSingleSlash {
            complete((gameActor ? GetAllPlayers).mapTo[List[Player]])
          }
      } ~
        post {
          entity(as[Player]) { player =>
            complete((gameActor ? AddPlayer(player)).map(_ => StatusCodes.OK))
          }
        } ~
        delete {
          entity(as[Player]) { player =>
            complete((gameActor ? RemovePlayer(player)).map(_ => StatusCodes.OK))
          }
        }
    }

  Http().newServerAt("localhost", 8000).bind(gameRoute)

}
