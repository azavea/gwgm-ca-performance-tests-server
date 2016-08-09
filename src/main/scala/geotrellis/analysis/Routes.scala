package geotrellis.analysis

import akka.http.scaladsl.server.Directives._

import geotrellis.analysis.status._

object Routes {
  def apply() =
    pathPrefix("system") {
      pathPrefix("status") {
        get {
          StatusService.system
        }
      }
    } ~
      pathPrefix("geomesa") {
        pathPrefix("status") {
          get {
            StatusService.geomesa
          }
        }
      }
  pathPrefix("geowave") {
    pathPrefix("status") {
      get {
        StatusService.geomesa
      }
    }
  }
}
