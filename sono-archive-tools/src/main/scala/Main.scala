import com.pixelmed.dicom.AttributeTag

import java.nio.file.Path
import com.typesafe.config.{Config, ConfigFactory}
import db.MongoWriterSample
import dicom.{DicomMetadataReader, LookupSQLiteFiles, QuerySQLiteFileForImagePathEntries}

import java.time.{LocalDate, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import scala.jdk.CollectionConverters.MapHasAsScala

object Main {

  val config: Config = ConfigFactory.load()
  val rootPath: String = config.getString("rootPath")

  def cliMain(args: Array[String]): Unit = {
    mongoSample()
  }

  def sqlLiteDbDump(): Unit = {


    println("printing contents of image files in the database")

    // call findDbFiles method and print each returned array element
    val dbFiles: List[Path] = LookupSQLiteFiles.findDbFiles(rootPath)
    dbFiles.foreach(println)
    dbFiles.foreach(path => {
      println(path.toString)
      val rows = QuerySQLiteFileForImagePathEntries.executeQuery(path.toString)
      if (rows.nonEmpty) {
        println(rows.head.keys.mkString(","))
        rows.foreach(row =>
          println(row.values.mkString(","))
        )
      }
    }
    )

  }

  // create bellow another method to call MongoWriterSample.writeToMongo method
  private def mongoSample(): Unit = {

    val DATE_TAG = new AttributeTag(0x0008, 0x0012)
    val NAME_TAG = new AttributeTag(0x0010, 0x0020)

    MongoWriterSample.runSample("")
    val dicomMeta = DicomMetadataReader
      .readAttributes("C:\\Users\\polar\\Downloads\\001\\canon\\110955\\A0000")
      .asScala
    println("done reading dicom metadata")

    val tagsToFilter = Set(
      DATE_TAG,
      NAME_TAG
    )
    import scala.jdk.CollectionConverters._

    val filteredDicomMeta = dicomMeta.filter { case (key, _) => tagsToFilter.contains(key) }
    filteredDicomMeta.foreach { case (key, value) =>

      val pattern = "<(.*?)>".r
      val matches = pattern.findAllIn(value.toString()).matchData.map(_.group(1)).toArray

      val zoned = if (isValidDateFormat(matches(2))) toZonedDateTime(matches(2)) else ""

      println(s"Key: $key, Value: $value, => ${matches(2)}  ${zoned}")
    }

  }

  private def isValidDateFormat(dateString: String): Boolean = {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    try {
      LocalDate.parse(dateString, formatter)
      true
    } catch {
      case _: DateTimeParseException => false
    }
  }

  private def toZonedDateTime(dateString: String): ZonedDateTime = {
    import java.time.{LocalDate, ZonedDateTime, ZoneId}
    import java.time.format.DateTimeFormatter

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val localDate = LocalDate.parse(dateString, formatter)
    val zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault())

    println(zonedDateTime.toString)
    zonedDateTime// This will print the ZonedDateTime
    }


}