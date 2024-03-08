package dicom

object QuerySQLiteFileForImagePathEntries {

  import java.sql.{Connection, DriverManager, ResultSet, Statement}

  var query =
    """
      |select se.SeriesKey, st.StudyKey,st.PKey, i.Filename , substring(i.datetime,1,10) as date, p.FirstName,p.LastName
      |from Series se
      |    join Study st using (StudyKey)
      |    join ImageRef i using(SeriesKey)
      |    join Patient p using (PKey)
      |""".stripMargin

  def executeQuery(filePath: String): Array[Map[String, Any]] = {
    // Load the SQLite JDBC driver
    Class.forName("org.sqlite.JDBC")

    // Establish a connection to the SQLite database
    val connection: Connection = DriverManager.getConnection(s"jdbc:sqlite:$filePath")
    val statement: Statement = connection.createStatement()

    // Execute the query
    val resultSet: ResultSet = statement.executeQuery(query)

    // Process the ResultSet and collect the results into an array
    val results = Iterator.continually((resultSet, resultSet.next)).takeWhile(_._2).map { case (rs, _) =>
      val metaData = rs.getMetaData
      (1 to metaData.getColumnCount).map(i => metaData.getColumnName(i) -> rs.getObject(i).asInstanceOf[Any]).toMap
    }.toArray

    // Close the ResultSet, Statement, and Connection
    resultSet.close()
    statement.close()
    connection.close()

    results
  }
}