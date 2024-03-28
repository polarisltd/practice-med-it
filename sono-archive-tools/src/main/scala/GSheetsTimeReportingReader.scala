

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{GoogleAuthorizationCodeFlow, GoogleClientSecrets}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.{Sheets, SheetsScopes}
import com.typesafe.config.{Config, ConfigFactory}
import db.TimeReportingPrakse

import java.io.{File, FileNotFoundException, IOException, InputStreamReader}
import java.time.format.DateTimeFormatter
import java.time.{LocalDate, YearMonth, ZoneId, ZonedDateTime}
import java.util.Collections
import scala.jdk.CollectionConverters._


class GSheetsTimeReportingReader{}
object GSheetsTimeReportingReader{

  val TOKENS_DIRECTORY_PATH = "tokens"
  val SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY)
  val CREDENTIALS_FILE_PATH = "/credentials.json"
  val APPLICATION_NAME = "Google Sheets API Java Quickstart"
  val JSON_FACTORY = GsonFactory.getDefaultInstance
  val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport
  val service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)).setApplicationName(APPLICATION_NAME).build

  def processTimeSheets() {


    val config: Config = ConfigFactory.load()
    val personSheetIds: List[String] = config.getStringList("sheetIds").asScala.toList
    val sheetRanges: List[String] = config.getStringList("sheetRanges").asScala.toList
    val configSheetIdRef = config.getString("configSheetIdRef")
    //val range = "Jan2024!C1:H14"

    personSheetIds.foreach(gSheetId => {

      val nameAndId = extractUserNameAndId(gSheetId, configSheetIdRef)
      val sheetTabName = getSheetName(LocalDate.now());
      val totalWorkedHours = sheetRanges.map(sheetRange => sumWorkHours(gSheetId, sheetTabName, sheetRange, nameAndId)).sum
      println(s">> ${nameAndId._1} (${nameAndId._2}) worked $totalWorkedHours hours in ${getSheetName(LocalDate.now())}")
      val key = s"$sheetTabName:${nameAndId._2}"
      val uuid = java.util.UUID.nameUUIDFromBytes(key.getBytes)
      val (firstDay, lastDay) = getFirstAndLastDayOfMonth(sheetTabName)

      GSheetsTimeReportingDAO.save(TimeReportingPrakse(
        uuid,
        sheetTabName,
        firstDay,
        lastDay,
        ZonedDateTime.now(),
        totalWorkedHours,
        nameAndId._1,
        nameAndId._2))
    })
  }

  def getFirstAndLastDayOfMonth(monthYear: String): (ZonedDateTime, ZonedDateTime) = {
    val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
    val yearMonth = YearMonth.parse(monthYear, formatter)

    val firstDayOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault())
    val lastDayOfMonth = yearMonth.atEndOfMonth().atStartOfDay(ZoneId.systemDefault())
    (firstDayOfMonth, lastDayOfMonth)
  }

def sumWorkHours(sheetId: String, sheetTabName: String, sheetRange: String, nameAndId: (String, Int)): Int = {
  val range = sheetRange.formatted(sheetTabName)
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

  def extractUserNameAndId(sheetId:String, configSheetIdRef: String): (String, Int) = {
    val response = service.spreadsheets.values.get(sheetId, configSheetIdRef).execute
    val values = response.getValues

    if (values != null && values.size() > 1) { // should have 2 rows
      val firstRow = values.get(0)
      val secondRow = values.get(1)
      if (firstRow != null && firstRow.size() > 0 && secondRow != null && secondRow.size() > 0
      ) {
        val userName = firstRow.get(0).toString
        val userId = secondRow.get(0).toString.toInt
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
