package part2_lowlevelserver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCode, StatusCodes, Uri}
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object LowLevelAPI extends App {

  implicit val system = ActorSystem()

  import scala.concurrent.ExecutionContext.Implicits.global

  //  val serverSource = Http().newServerAt("localhost", 8000).connectionSource()
  //  val connectionSink = Sink.foreach[IncomingConnection] { connection =>
  //    println(s"Accepted incoming connection from: ${connection.remoteAddress}")
  //  }
  //
  //  val serverBindingFuture = serverSource.to(connectionSink).run()
  //
  //  serverBindingFuture.onComplete {
  //    case Success(binding) =>
  //      println("Server binding successfully")
  //    //      binding.terminate(2.second)
  //    case Failure(exception) => println(s"Server binding failed $exception")
  //  }

  /**
   * Method 1: synchronously
   */

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(HttpMethods.GET, uri, headers, entity, protocol) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
          """
            |<h1>Hello World</h1>
            |""".stripMargin)
      )


    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>Route Not Found</h1>
          |""".stripMargin))

  }

  //  val httpSyncConnectionHandler = Sink.foreach[IncomingConnection] { connection =>
  //    connection.handleWithSyncHandler(requestHandler)
  //  }

  //  Http().newServerAt("localhost", 8000).connectionSource().runWith(httpSyncConnectionHandler)
  //  Http().newServerAt("localhost", 8000).bindSync(requestHandler)

  /**
   * Method 2: serve back HTTP responses asynchronously
   */
  val asyncRequestHandler: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), headers, entity, protocol) =>
      Future(HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
          """
            |<h1>Hello World</h1>
            |""".stripMargin)
      ))


    case request: HttpRequest =>
      request.discardEntityBytes()
      Future(HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>Route Not Found</h1>
          |""".stripMargin)))

  }

  //  Http().newServerAt("localhost", 8000).bind(asyncRequestHandler)

  /**
   * method #3: async Via Akka Streams
   */

  val streamRequestHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/home"), headers, entity, protocol) =>
      HttpResponse(
        StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
          """
            |<h1>Hello World</h1>
            |""".stripMargin)
      )


    case request: HttpRequest =>
      request.discardEntityBytes()
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>Route Not Found</h1>
          |""".stripMargin))
  }

  //    Http().newServerAt("localhost", 8000).bindFlow(streamRequestHandler)

  /**
   * Exercise: Create you own server http on localhost 8388, wich replies
   * - with a welcome message on the front door localhost:8388
   * - with a proper HTML in localhost:8388/bout
   * - with a 404 message otherwise
   */

  val streamRequestHandler2: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map {
    case HttpRequest(HttpMethods.GET, Uri.Path("/about"), headers, entity, protocol) =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>About</h1>
          |""".stripMargin))
    case HttpRequest(HttpMethods.GET, Uri.Path("/"), headers, entity, protocol) =>
      HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>Welcome</h1>
          |""".stripMargin))
    case HttpRequest(method, url, headers, entity, protocol) =>
      HttpResponse(StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`,
        """
          |<h1>Not found</h1>
          |""".stripMargin))

  }

  Http().newServerAt("localhost", 8388).bindFlow(streamRequestHandler2)

}
