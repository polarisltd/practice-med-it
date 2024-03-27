package db

import java.time.ZonedDateTime
import java.util.UUID

case class TimeReportingPrakse(_id: UUID,
                               timePeriod: String,
                               startDate: ZonedDateTime,
                               endDate: ZonedDateTime,
                               createdAt: ZonedDateTime,
                               hoursWorked: Int,
                               employeeName: String,
                               employeeId: Int)
