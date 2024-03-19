package dicom

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import db.MongoWriterSample
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{GET, POST, Path, Produces}
import swagger.DefaultJsonFormats

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


  class DicomService (implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats{

  implicit val timeout: Timeout = Timeout(2.seconds)

    val system: ActorSystem = ActorSystem("SonoArchiveSystem")
    // Create the actor
    val processorActor: ActorRef = system.actorOf(ArchiveProcessorActor.props, "processorActor")


    val route: Route =
    getMongoSample ~
      postArchiveCanon

  @GET
  @Path("/mongoexample1")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(
    summary = "this call creates single record in mongodb and retrieves it",
    description = "try out mongo writer from scala")
  def getMongoSample: Route =
    path("mongoexample1") {
      get {
          complete {
            //  (hello ? Hello(name)).mapTo[Greeting]
            MongoWriterSample.runSample()
            s"[\"OK\"]"
          }
        }


    }

    object LocationType extends Enumeration {
      type LocationType = Value
      val Voluson, Canon, Philyps = Value
    }

    case class PostRequest(path: String, locationType: String)

    case class PostResponse(patient: String, dateAt: String, filePath: String)

    implicit val postRequestFormat = jsonFormat2(PostRequest)
    implicit val postResponseFormat = jsonFormat3(PostResponse)

  @POST
  @Path("/archive/canon")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "New POST endpoint",
    description = "New POST endpoint",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[PostRequest])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Successful operation",
        content = Array(new Content(schema = new Schema(implementation = classOf[PostResponse])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def postArchiveCanon: Route =
    path("archive" / "canon") {
      post {
        entity(as[PostRequest]) { request =>
          complete {
            println(request)
            processorActor ! request.path
            PostResponse("canon archive processing", "started", request.path)
          }
        }
      }
    }


}
