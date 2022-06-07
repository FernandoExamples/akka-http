package recap

import scala.concurrent.Future
import scala.util.{Failure, Success}

object ScalaRecap extends App {
  val aCondition: Boolean = false

  def myFunction(x: Int) = {
    if (x > 4) 42 else 65
  }

  //instructions vs expressions
  //types + types inference

  //OO features
  class Animal

  trait Carnivore {
    def eat(a: Animal): Unit
  }

  object Carnivore

  //Generics
  abstract class MyList[+A]

  //math notations
  1 + 2
  1.+(2)

  //FP
  val incrementer: Function[Int, Int] = (x: Int) => x + 1
  val incrementer2: Int => Int = (x: Int) => x + 1
  val incrementer3 = (x: Int) => x + 1

  List(1, 2, 3).map(incrementer)
  //High Order Functions: Flatmap, filter

  //For-comprehensions
  //Monads: Options, Try
  //Pattern matching

  val unknown: Any = 2
  val order = unknown match {
    case 1 => "One"
    case 2 => "Two"
    case _ => unknown
  }

  try {
    //code can thrown
    throw new RuntimeException
  } catch {
    case e: RuntimeException => println("Fail")
  }

  /**
   * Scala advanced
   */

  //multithreading fundamentals

  import scala.concurrent.ExecutionContext.Implicits.global

  val future = Future {
    //long computations
    //executed on SOME other thread
    42
  }
  //map, flatMap, filter ...

  future.onComplete {
    case Success(value) => println("I found the meaning of life")
    case Failure(exception) => println("Failure")
  }

  val partialFunction: PartialFunction[Int, Int] = {
    case 1 => 42
    case 2 => 65
    case _ => 9999
  }

  //type Aliases
  type AkkaReceive = PartialFunction[Any, Unit]

  def receive: AkkaReceive = {
    case 1 => println("Hello")
    case _ => println("Confused")
  }

  //Implicits
  implicit val timeout = 3000

  def setTimeout(f: () => Unit)(implicit timeout: Int) = f()

  setTimeout(() => println("Timeout"))

  //conversions
  // 1) implicit methods
  case class Person(name: String) {
    def greet: String = s"Hi my name is $name"

  }

  implicit def fromStringToPerson(name: String): Person = Person(name)

  "Peeter".greet
  //fromStringToPerson("Petter").greet

  //2) implicit classes
  implicit class Dog(name: String) {
    def bark = println("Bark!")
  }

  "Lassie".bark

  //implicit organizations
  //local scope
  implicit val numberOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  List(1, 2, 3).sorted //List (3,2,1)

  //companion objects of the types involved in the call
  object Person {
    implicit val personOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  List(Person("Bob"), Person("Allice")).sorted //(Person.personOrdering is the implicit here)


}
