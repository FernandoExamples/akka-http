package recap

import akka.actor.ActorSystem
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.util.{Failure, Success}

object StreamsRecap extends App {
  implicit val system = ActorSystem("AkkaStreamRecap")

  val source = Source(1 to 100)

  val sink = Sink.foreach(println)
  val flow = Flow[Int].map(x => x + 1)

  val runnableGraph = source.via(flow).to(sink)
  val simpleMaterializedValue = runnableGraph.run()

  //MATERIALIZED VALUE
  val sumSink = Sink.fold[Int, Int](0)((a, b) => a + b)
  val sumFuture = source.runWith(sumSink)

  import scala.concurrent.ExecutionContext.Implicits.global

  sumFuture.onComplete({
    case Success(value)     => println(s"Future $value")
    case Failure(exception) => println("Failure")
  })

  val anotherMaterializedValue = {
    source.viaMat(flow)(Keep.right).toMat(sink)(Keep.left).run()

    /**
      * 1 - Materializing a graph means materializing all the components
      * 2 - Materializing value can be ANYTHING AT ALL
      */
    /**
      * Buffer elements
      * apply a strategy in case the buffer overflows
      * fail the entire stream
      */
    val bufferFlow = Flow[Int].buffer(10, OverflowStrategy.dropHead)

    source.async.via(bufferFlow).runForeach { e =>
      Thread.sleep(100)
      println(e)

    }
  }

}
