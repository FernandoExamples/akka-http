package part3_highlevelserver

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.server.Directives._


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

  var players = Map[String, Player]();

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
      sender() ! OperationSucces

    case RemovePlayer(player) =>
      log.info(s"Trying to remove $player")
      players = players - (player.nickname)
      sender() ! OperationSucces
  }
}


object MarshallingJSON extends App {
  implicit val system = ActorSystem("MarshallingJSON")

  import GameAreaMap._
  import system.dispatcher

  val rtjvmMap = system.actorOf(Props[GameAreaMap], "rockTheJVMAreaMap")
  val playerList = List(
    Player("martin", "Warrior", 70),
    Player("roland", "Eld", 67),
    Player("fer", "Wizard", 90),
  )

  playerList.foreach { player =>
    rtjvmMap ! AddPlayer(player)
  }

  /**
   *
   */
  val rtjvmGameRoute =
    pathPrefix("api" / "player") {
      get {
        path("class" / Segment) { characterClass =>
          //TODO all players
          ???
        } ~
          (path(Segment) | parameter("nickname")) { nickname =>
            ???
            //TODO player by nickname
          } ~
          pathEndOrSingleSlash {
            //TODO get all
            ???
          }
      } ~
        post {
          ???
        } ~
        delete {
          ???
        }
    }

}
