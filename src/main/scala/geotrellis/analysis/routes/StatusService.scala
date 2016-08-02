package geotrellis.analysis.routes

import java.lang.management.ManagementFactory
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import scala.concurrent.duration._

import geotrellis.analysis.model._
import geotrellis.analysis._

object StatusService extends BaseService with System.LoggerExecutor {
  def uptime = {
    log.info("/status/uptime executed")
    complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString()))
  }
}
