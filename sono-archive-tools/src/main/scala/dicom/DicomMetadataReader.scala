package dicom

import com.pixelmed.dicom.{AttributeList, DicomException}

import java.io.{File, IOException}
import java.nio.file.Path

object DicomMetadataReader {

  private val attributeList = new AttributeList

  @throws[DicomException]
  @throws[IOException]
  def readAttributes(dcmFilePath: String): AttributeList = {
    attributeList.read(new File(dcmFilePath))
    attributeList
  }

  @throws[DicomException]
  @throws[IOException]
  def readAttributes(dcmFilePath: Path): AttributeList = {
    attributeList.read(dcmFilePath.toFile)
    attributeList
  }

}
