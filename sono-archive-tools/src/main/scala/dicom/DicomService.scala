package dicom

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
import spray.json._
import swagger.DefaultJsonFormats

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt


  class DicomService (implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats{

  implicit val timeout: Timeout = Timeout(2.seconds)


  val route: Route =
    getCanon ~
      postCanon

  @GET
  @Path("/dicom/canon")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "search canon archive path and extract dicom metadata",
    description = "return file paths and metadata",
    parameters = Array(
      new Parameter(name = "rootPath", in = ParameterIn.QUERY, description = "lookup base path")),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Hello response",
        content = Array(new Content(schema = new Schema(implementation = classOf[String])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def getCanon: Route =
    path("dicom" / "canon") {
      get {
        parameter(Symbol("rootPath")) { rootPath =>
          complete {
            //  (hello ? Hello(name)).mapTo[Greeting]
            MongoWriterSample.runSample(rootPath)
            s"[$rootPath]"
          }
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
  @Path("/dicom/canon")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "New POST endpoint",
    description = "New POST endpoint",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[PostRequest])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Successful operation",
        content = Array(new Content(schema = new Schema(implementation = classOf[PostResponse])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def postCanon: Route =
    path("dicom" / "canon") {
      post {
        entity(as[PostRequest]) { request =>
          complete {
            println(request)
            // TODO: Implement the endpoint's logic here
            // For now, we'll just return a dummy response
            PostResponse("dummyPatient", "20240301", "dummyFilePath")
          }
        }
      }
    }


}
