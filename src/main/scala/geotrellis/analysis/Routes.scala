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
          parameters('table) { table =>
            get {
              StatusService.geomesa(table)
            }
          }
        } ~
          pathPrefix("sfts") {
            pathEndOrSingleSlash {
              get {
                geomesa.SimpleFeatureTypes.list
              }
            } ~
              pathPrefix(Segment) { sftName =>
                get {
                  geomesa.SimpleFeatureTypes.detail(sftName)
                }
              }
          }
      } ~
      pathPrefix("geowave") {
        pathPrefix("status") {
          parameters('table) { table =>
            get {
              StatusService.geowave(table)
            }
          }
        } ~
          pathPrefix("sfts") {
            pathEndOrSingleSlash {
              get {
                geowave.SimpleFeatureTypes.list
              }
            } ~
              pathPrefix(Segment) { sftName =>
                get {
                  geowave.SimpleFeatureTypes.detail(sftName)
                }
              }
          }
      }
}
