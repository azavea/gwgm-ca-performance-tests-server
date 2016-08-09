package geotrellis.analysis.status

import java.lang.management.ManagementFactory
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import scala.concurrent.duration._

import geotrellis.analysis._
import geotrellis.analysis.geomesa.connection._
import geotrellis.analysis.geowave.connection._

object StatusService extends BaseService
    with GeoMesaConnection
    with GeoWaveConnection
    with System.LoggerExecutor {
  def system = {
    log.info("/status/uptime executed")
    complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString()))
  }

  def geomesa(table: String) = {
    log.info("/status/geomesa executed")
    complete(gwBasicOperations.getRowCount(table))
  }

  def geowave(table: String) = {
    log.info("/status/geowave executed")
    complete(gwBasicOperations.getRowCount(table))
  }
}
