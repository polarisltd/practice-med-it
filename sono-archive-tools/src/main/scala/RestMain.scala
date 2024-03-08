

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.RouteConcatenation
import dicom.DicomService
import swagger.SwaggerDocService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object RestMain extends App with RouteConcatenation {
  implicit val system: ActorSystem = ActorSystem("mySystem")
  sys.addShutdownHook(system.terminate())

  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  val routes =
    new DicomService().route ~
    SwaggerDocService.routes

  val f = for {
    bindingFuture <- Http().newServerAt("0.0.0.0", 8084).bind(routes)
    waitOnFuture  <- Future.never
  } yield waitOnFuture

  Await.ready(f, Duration.Inf)
}
