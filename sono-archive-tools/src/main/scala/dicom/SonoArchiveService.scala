package dicom


import java.nio.file.{Files, Path, Paths}
import java.time.{LocalDate, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.stream.Collectors
import scala.jdk.CollectionConverters._
import com.pixelmed.dicom.{AttributeList, AttributeTag}
import db.DB
import org.mongodb.scala.{Observable, result}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
object SonoArchiveService {

  val DATE_TAG = new AttributeTag(0x0008, 0x0012)
  val NAME_TAG = new AttributeTag(0x0010, 0x0020)

  import java.util.UUID

  case class Patient(patientId: UUID, name: String, createdAt: ZonedDateTime, path: List[String]) {
//    def this(name: String, createdAt: ZonedDateTime, path: Array[String]) {
//      this(UUID.nameUUIDFromBytes((name + createdAt.toString).getBytes), name, createdAt, path)
//    }

  }

  def processCanonArchive(root: String): Unit = {
    val files = findCanonFiles(root)
    files.foreach { file =>
      val attributes = DicomMetadataReader.readAttributes(file)
      val patient = processAttributeList(file, attributes)
      PatientDAO.save(patient)
      println(s" Patient: ${patient.name} created at: ${patient.createdAt} from file: ${patient.path}")
    }



    val patients: Seq[Patient] = PatientCache.getAll
    val insertObservable: Observable[result.InsertManyResult] = DB.patient.insertMany(patients).toObservable()
    insertObservable.subscribe(
      (result) => println(s"Insert operation completed successfully. numdocs=${patients.length}"),
      (error: Throwable) => println(s"Insert operation failed with error: ${error.getMessage}")
    )

//    PatientCache.getAll.foreach { patient =>
//      val futureResult = DB.patient.insertOne(patient).toFuture()
//      Await.result(futureResult, 10.seconds) // Adjust the timeout as needed
//    }


  }

  def findCanonFiles(root: String): List[Path] = {
    val dbFiles = Files.walk(Paths.get(root))
      .filter(Files.isRegularFile(_))
      .filter(path => path.getFileName.toString.matches("A\\d{4}"))
      .collect(Collectors.toList[Path])
      .asScala
      .toList

    dbFiles
  }

  private def processAttributeList(path: Path, dicomMeta: AttributeList): Patient = {

    val dateAttr = dicomMeta.asScala.filter { case (key, _) => DATE_TAG.equals(key) }.values.headOption

    val nameAttr = dicomMeta.asScala.filter { case (key, _) => NAME_TAG.equals(key) }.values.headOption

    val pattern = "<(.*?)>".r

    val createdAt : ZonedDateTime = dateAttr match {
      case Some(value) => {
        val matches = pattern.findAllIn(value.toString()).matchData.map(_.group(1)).toArray
        toZonedDateTime(matches(2))
      }
      case None => null
    }

    val name = nameAttr match {
      case Some(value) => {
        val matches = pattern.findAllIn(value.toString()).matchData.map(_.group(1)).toArray
        matches(2)
      }
      case None => null
    }
    Patient(UUID.nameUUIDFromBytes((name + createdAt.toString).getBytes),
      name, createdAt, List(path.toAbsolutePath.toString))
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
    import java.time.{LocalDate, ZoneId}
    import java.time.format.DateTimeFormatter

    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val localDate = LocalDate.parse(dateString, formatter)
    val zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault())

    zonedDateTime // This will print the ZonedDateTime
  }

}
