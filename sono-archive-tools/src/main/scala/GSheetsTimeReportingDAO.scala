import dicom.SonoArchiveService.Patient
import db.{DB, TimeReportingPrakse}
import dicom.SonoArchiveService.Patient
import org.mongodb.scala.Observer
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, ReplaceOptions, Updates}

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.{Duration, DurationInt}
import scala.util.{Failure, Success}


object GSheetsTimeReportingDAO {


  def save_(timeReportingPrakse: TimeReportingPrakse): Unit = {
    val filter = Filters.eq("_id", timeReportingPrakse._id)
    val options = new FindOneAndUpdateOptions().upsert(true)

    val update = Updates.combine(
      Updates.set("hoursWorked", timeReportingPrakse.hoursWorked),
    )

    val t = DB.timeReportingPrakse.findOneAndUpdate(filter, update, options)

      t.subscribe(new Observer[TimeReportingPrakse] {
      override def onNext(result: TimeReportingPrakse): Unit = {
        println(s"TimeReporting: ${result.toString} updated or inserted")
      }

      override def onError(e: Throwable): Unit = {
        println(s"Error: ${e.getMessage}")
      }

      override def onComplete(): Unit = {
        println("Completed!")
      }
    })

    val result = Await.result(t.toFuture(), 10.seconds) // adjust the duration as needed

    println(s"Done")

  }
  import scala.concurrent.ExecutionContext.Implicits.global

  def save(timeReportingPrakse: TimeReportingPrakse): Unit = {
    val filter = Filters.eq("_id", timeReportingPrakse._id)
    val options = new ReplaceOptions().upsert(true)

    val futureResult = DB.timeReportingPrakse.replaceOne(filter, timeReportingPrakse, options).toFuture()
    futureResult.onComplete {
      case Success(result) => println(s"TimeReporting: ${result.toString} updated or inserted")
      case Failure(e) => println(s"Error: ${e.getMessage}")
    }
    Await.result(futureResult, 10.seconds)
  }


}
