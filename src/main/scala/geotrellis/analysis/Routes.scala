package geotrellis.analysis

import akka.http.scaladsl.server.Directives._

import geotrellis.analysis.status._

object Routes {
  def apply() =
    pathPrefix("status") {
      get {
        StatusService.status
      }
    }
}
