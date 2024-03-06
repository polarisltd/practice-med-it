import com.pixelmed.dicom.{AttributeList, DicomException}

import java.io.{File, IOException}

object DicomMetadataReader {

  private val attributeList = new AttributeList

  @throws[DicomException]
  @throws[IOException]
  def readAttributes(dcmFilePath: String): AttributeList = {
    attributeList.read(new File(dcmFilePath))
    attributeList
  }

}
