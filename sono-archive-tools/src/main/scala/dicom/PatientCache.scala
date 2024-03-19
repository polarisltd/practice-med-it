package dicom


import dicom.SonoArchiveService.Patient

import scala.collection.mutable

object PatientCache {
  private val cache: mutable.HashMap[String, Patient] = mutable.HashMap()

  def getAll: List[Patient] = cache.values.toList
  def get(key: String): Option[Patient] = cache.get(key)

  def put(key: String, value: Patient): Unit = cache.put(key, value)

  def contains(key: String): Boolean = cache.contains(key)

  def update(key: String, value: Patient): Unit = cache.update(key, value)

  def save(patient: Patient): Unit = {
    val key = patient.name + patient.createdAt.toString

      get(key) match {
        case Some(existingPatient) =>
          try {
            val updatedPatient = patient.copy(path = existingPatient.path ++ patient.path)
            cache.update(key, updatedPatient)
          } catch {
            case e: Exception => e.printStackTrace()
          }
        case None => put(key, patient)
      }
  }
  def size() : Int = cache.size
}
