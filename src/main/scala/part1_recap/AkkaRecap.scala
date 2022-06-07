package part1_recap

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{
  Actor,
  ActorLogging,
  ActorSystem,
  OneForOneStrategy,
  PoisonPill,
  Props,
  Stash,
  SupervisorStrategy
}
import akka.util.Timeout

object AkkaRecap extends App {

  class SimpleActor extends Actor with Stash with ActorLogging {

    override def receive: Receive = {
      case "createChild" =>
        val childActor = context.actorOf(Props[SimpleActor], "simpleActor")
        childActor ! "Hello"
      case "change"    => context.become(anotherHandler)
      case "stashThis" => stash()
      case "change handler now" =>
        unstashAll()
        context.become(anotherHandler)
      case message => log.info("I received message")
    }

    def anotherHandler: Receive = {
      case message => println(s"In another receive handler: $message")
    }

    override def preStart(): Unit = {
      log.info("I'm starting")
    }

    override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy() {
      case _: RuntimeException => Restart
      case _                   => Stop
    }
  }

  //actor encapsulation
  val system = ActorSystem("Akkarecap")

  // #1 only can instantiate an actor through ActorSystem
  val simpleActor = system.actorOf(Props[SimpleActor])

  //#2 sendind messages
  simpleActor ! "Hello"

  /**
    * - messages are sent asynchronously
    * - many actors (in the millions) can share a few dozen threads
    * - each message os processed/handled ATOMICALLY
    * -- no need for locks
    */
  //actors can spawn other actors
  //guardians: /system, /user, / = root guardian

  //actors has a defined lifecycle: started, stopped, suspended, resumed, restarted

  //stopping actors - context.stop
  simpleActor ! PoisonPill

  //logging
  //supervision

  //Configure Akka infrastructure: dispatchers, routers, mailboxes

  //schedulers
  import scala.concurrent.duration._
  import system.dispatcher

  system.scheduler.scheduleOnce(2.seconds) {
    simpleActor ! "Delaye happy birthday"
  }

  //akka patterns:  FSM + ask pattern
  import akka.pattern.ask
  implicit val timeout = Timeout(3.seconds)

  val future = simpleActor ? "question"

  import akka.pattern.pipe
  val anotherActor = system.actorOf(Props[SimpleActor], "anotherActor")
  future.mapTo[String].pipeTo(anotherActor)

}
