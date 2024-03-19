package db

import com.mongodb.client.model.Accumulators.sum
import com.mongodb.client.model.Aggregates.group
import com.mongodb.client.model.Filters.{and, gte, lte}
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Aggregates.filter

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class HistoryVisit(visitId: UUID,
                name: String,
                source: String,
                createdAt: ZonedDateTime)

object MongoWriterSample {

  def runSample(): Unit = {

    val createdAt = LocalDateTime.now().atZone(ZoneId.systemDefault())
    val uuid = java.util.UUID.nameUUIDFromBytes(createdAt.toString.getBytes)

    val historyVisit = HistoryVisit(uuid, "sqLiteFilePath", "source", createdAt)

    // https://medium.com/@qyang.nie/introduction-of-mongodb-scala-driver-8d19e4658c9b
    Await.result(DB.historyVisit.insertOne(historyVisit).toFuture(), Duration.create(10, "seconds"))

    selectGroupByNameCountWhereBetweenCreatedAt(createdAt.minusDays(7), createdAt)

  }

  /**
   * Bellow is sql equivalent:
   * select count(*),name
   * group by name
   * where createdAt between start and end
   * @param start
   * @param end
   */
  private def selectGroupByNameCountWhereBetweenCreatedAt(start: ZonedDateTime, end:ZonedDateTime): Unit = {
    val pipeline: Seq[Bson] = Seq(
      filter(and(gte("createdAt", start), lte("createdAt", end))),
      group("$name", sum("count", 1))
    )

    val result: AggregateObservable[Document] = DB.historyVisit.aggregate(pipeline)

    result.subscribe(new Observer[Document] {
      override def onNext(result: Document): Unit = println("OK" + result.toJson())
      override def onError(e: Throwable): Unit = println("Failed" + e.getMessage)
      override def onComplete(): Unit = println("Completed")
    })  }



}
