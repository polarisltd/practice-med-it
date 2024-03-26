

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange

import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.Collections
import java.util
import java.io.File
import scala.collection.convert.ImplicitConversions.`collection AsScalaIterable`
import scala.jdk.CollectionConverters._
import com.typesafe.config.{Config, ConfigFactory}

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class GSheetsTimeReportingReader{}
object GSheetsTimeReportingReader extends App{
  private val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  private val JSON_FACTORY = GsonFactory.getDefaultInstance
  private val TOKENS_DIRECTORY_PATH = "tokens"

  private val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)
  private val CREDENTIALS_FILE_PATH = "/credentials.json"

  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
  val config: Config = ConfigFactory.load()
  private val personSheetIds: List[String] = config.getStringList("sheetIds").asScala.toList
  private val sheetNames: List[String] = config.getStringList("sheetNames").asScala.toList
  private val configSheetIdRef = config.getString("configSheetIdRef")
  //val range = "Jan2024!C1:H14"
  val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build

  personSheetIds.foreach(sheetId => {

    val nameAndId = extractUserNameAndId(sheetId)

    val totalWorkedHours = sheetNames.map(sheetName => sumWorkHours(sheetId, sheetName, nameAndId)).sum
    println(s">> ${nameAndId._1} (${nameAndId._2}) worked $totalWorkedHours hours in ${getSheetName(LocalDate.now())}")
    totalWorkedHours
  })

def sumWorkHours(sheetId: String, sheetName: String, nameAndId: (String, String)): Int = {
  val range = sheetName.formatted(getSheetName(LocalDate.now()))
  println(s"reading sheet: $range")
  val response = service.spreadsheets.values.get(sheetId, range).execute
  val values = response.getValues
  if (values == null || values.isEmpty) {
    println(s"No data found for sheet $nameAndId._1 in $range")
    0
  } else {
    val totalSum = values.asScala.map(row => row.asScala.map(value => if (value.toString.trim.isEmpty || !value.toString.trim.matches("-?\\d+")) 0 else value.toString.trim.toInt).sum).sum
    println(s"${nameAndId._1} worked $totalSum hours in $range")
    totalSum
  }
}

  def getSheetName(currentDate: LocalDate): String = {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
    val formattedDate = currentDate.format(formatter)
    formattedDate
  }

  case class EmptySheetRange(message: String) extends Exception(message)

  def extractUserNameAndId(sheetId:String): (String, String) = {
    val response = service.spreadsheets.values.get(sheetId, configSheetIdRef).execute
    val values = response.getValues

    if (values != null && values.size() > 1) { // should have 2 rows
      val firstRow = values.get(0)
      val secondRow = values.get(1)
      if (firstRow != null && firstRow.size() > 0 && secondRow != null && secondRow.size() > 0
      ) {
        val userName = firstRow.get(0).toString
        val userId = secondRow.get(0).toString
        (userName, userId)
      } else {
        throw EmptySheetRange(s"The first row does not have enough columns. $sheetId, $configSheetIdRef ")
      }
    } else {
      throw EmptySheetRange(s"No data found.  $sheetId, $configSheetIdRef")
    }
  }


  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  @throws[IOException]
  private def getCredentials(HTTP_TRANSPORT: NetHttpTransport) = {
    // Load client secrets.
    val in = classOf[GSheetsTimeReportingReader].getResourceAsStream(CREDENTIALS_FILE_PATH)
    if (in == null) throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
    val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))
    // Build flow and trigger user authorization request.
    val flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build
    val receiver = new LocalServerReceiver.Builder().setPort(8888).build
    new AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
  }

  /**
   * Prints the names and majors of students in a sample spreadsheet:
   * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
   */

}
