package part2_lowlevelserver.guitarsrest

import akka.actor.{Actor, ActorLogging}

case class Guitar(make: String, model: String, quantity: Int = 0)

object GuitarDB {
  case class CreateGuitar(guitar: Guitar)

  case class GuitarCreated(id: Int)

  case class FindGuitarById(id: Int)

  case object FindAllGuitars

  case class AddQuantity(id: Int, quantity: Int)

  case class FindGuitarsInStock(inStock: Boolean)
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

    case AddQuantity(id, quantity) =>
      log.info(s"Trying to add $quantity items for guitar $id")
      val guitar = guitars.get(id)
      val newGuitar = guitar.map {
        case Guitar(make, model, q) => Guitar(make, model, q + quantity)
      }

      newGuitar.foreach(g => guitars = guitars + (id -> g))
      sender() ! newGuitar

    case FindGuitarsInStock(inStock) =>
      log.info(s"Searching for all guitars ${if (inStock) "in" else "out of"} stock")
      if (inStock)
        sender() ! guitars.values.filter(_.quantity > 0)
      else
        sender() ! guitars.values.filter(_.quantity == 0)

  }
}