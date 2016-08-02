package geotrellis.analysis

import akka.http.scaladsl.server.Directives._

import geotrellis.analysis.routes._

object Routes {
  def apply() =
    pathPrefix("status") {
      pathPrefix("uptime") {
        get {
          StatusService.uptime
        }
      }
    }
}
