package dicom

import db.DB
import dicom.SonoArchiveService.Patient
import org.mongodb.scala.Observer
import org.mongodb.scala.bson.BsonDateTime
import org.mongodb.scala.model.{Filters, FindOneAndUpdateOptions, Updates}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object PatientDAO {

  def save_(patient: Patient): Unit = {
    Await.result(DB.patient.insertOne(patient).toFuture(), Duration.create(10, "seconds"))
  }

  def save(patient: Patient): Unit = {
    val filter = Filters.eq("patientId", patient.patientId)
    val options = new FindOneAndUpdateOptions().upsert(true)

    val update = Updates.combine(
      Updates.set("name", patient.name),
      Updates.set("createdAt", BsonDateTime(patient.createdAt.toInstant.toEpochMilli)),
      Updates.push("path", patient.path) // Use the $push operator to append the path
    )

    DB.patient.findOneAndUpdate(filter, update, options).subscribe(new Observer[Patient] {
      override def onNext(result: Patient): Unit = {
        println(s"Patient: ${result.name} updated or inserted")
      }

      override def onError(e: Throwable): Unit = {
        println(s"Error: ${e.getMessage}")
      }

      override def onComplete(): Unit = {
        println("Completed!")
      }
    })
  }


}
