

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import com.typesafe.config.ConfigFactory
import dicom.DicomService
import swagger.SwaggerDocService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object RestMain extends App with RouteConcatenation {
  implicit val system: ActorSystem = ActorSystem("mySystem")
  sys.addShutdownHook(system.terminate())

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  val startup = new Startup(config)(system, executionContext)

  val routes =
    new DicomService().route ~
    SwaggerDocService.routes

  val f = for {
    bindingFuture <- Http().newServerAt(host, port).bind(routes)
    waitOnFuture  <- Future.never
  } yield waitOnFuture

  println(s"Server online at http://$host:$port")

  Await.ready(f, Duration.Inf)
}
