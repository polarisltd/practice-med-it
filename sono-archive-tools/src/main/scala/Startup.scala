import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.inject.ApplicationLifecycle

import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Startup @Inject() (config: Config)(implicit system: ActorSystem, ec: ExecutionContext) {
  private val processTimeSheetsConfig = config.getConfig("scheduling.processTimeSheets")
  val hour: Int = processTimeSheetsConfig.getInt("hour")
  val minute: Int = processTimeSheetsConfig.getInt("minute")

  import java.time.{LocalTime, Duration, ZonedDateTime}

  println(s"Scheduling processTimeSheets at $hour:$minute")

  val now: ZonedDateTime = ZonedDateTime.now()
  private val nextRun = now.withHour(hour).withMinute(minute)
  private val initialDelay = if (now.compareTo(nextRun) < 0)
    Duration.between(now, nextRun)
  else
    Duration.between(now, nextRun.plusDays(1))

  private val interval = java.time.Duration.ofHours(24)

  private val initialDelayDuration = FiniteDuration(initialDelay.toNanos, NANOSECONDS)
  private val intervalDuration = FiniteDuration(interval.toNanos, NANOSECONDS)

    system.scheduler.scheduleAtFixedRate(initialDelayDuration, intervalDuration)(new Runnable {
      def run(): Unit = GSheetsTimeReportingReader.processTimeSheets()
    })
}
