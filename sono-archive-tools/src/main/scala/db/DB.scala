package db

import com.typesafe.config.{Config, ConfigFactory}
import dicom.SonoArchiveService.Patient
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecRegistries, CodecRegistry}
import org.bson.codecs.{Codec, DecoderContext, EncoderContext, UuidCodec}
import org.bson.{BsonReader, BsonType, BsonWriter, UuidRepresentation}
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection}

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DB {

  val config: Config = ConfigFactory.load()
  private val mongoConnectString: String = config.getString("mongoConnectString")
  private val mongoDatabase: String = config.getString("mongoDatabase")


  private val zonedDateTimeCodec: Codec[ZonedDateTime] =
    new Codec[ZonedDateTime] {

      override def decode(reader: BsonReader,
                          decoderContext:
                          DecoderContext): ZonedDateTime =
        ZonedDateTime.parse(reader.readString())

      override def encode(writer: BsonWriter,
                          value: ZonedDateTime,
                          encoderContext: EncoderContext): Unit =
        writer
          .writeString(
            value.format(DateTimeFormatter.ISO_DATE_TIME))

      override def getEncoderClass: Class[ZonedDateTime] =
        classOf[ZonedDateTime]

    }

  private val customCodecs: CodecRegistry =
    fromProviders(classOf[HistoryVisit], classOf[Patient], classOf[TimeReportingPrakse])

  private val javaCodecs =
    CodecRegistries.fromCodecs(
      zonedDateTimeCodec,
      new UuidCodec(UuidRepresentation.STANDARD))

  private val codecRegistry =
    fromRegistries(
      customCodecs,
      javaCodecs,
      DEFAULT_CODEC_REGISTRY)

  private val database =
    MongoClient(mongoConnectString)
      .getDatabase(mongoDatabase)
      .withCodecRegistry(codecRegistry)

  val historyVisit: MongoCollection[HistoryVisit] =
    database.getCollection[HistoryVisit]("visits")

  val patient: MongoCollection[Patient] =
    database.getCollection[Patient]("patients")

  val timeReportingPrakse : MongoCollection[TimeReportingPrakse] =
    database.getCollection[TimeReportingPrakse]("timeReportingPrakse")


}
