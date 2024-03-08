package dicom

object LookupSQLiteFiles {
  
  import java.nio.file.{Files, Path, Paths}
  import java.util.stream.Collectors
  import scala.jdk.CollectionConverters._

  def findDbFiles(root: String): List[Path] = {
    val dbFiles = Files.walk(Paths.get(root))
      .filter(Files.isRegularFile(_))
      .filter(_.getFileName.toString.endsWith(".db"))
      .collect(Collectors.toList[Path])
      .asScala
      .toList

    dbFiles
  }  
  
  
}
