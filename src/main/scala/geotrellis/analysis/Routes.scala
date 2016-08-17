package geotrellis.analysis

import akka.http.scaladsl.server.Directives._

import geotrellis.analysis.status._
import geotrellis.analysis.geomesa.connection._
import geotrellis.analysis.geowave.connection._

object Routes {

  val systemRoutes =
    pathPrefix("system") {
      pathPrefix("status") {
        pathEndOrSingleSlash {
          get {
            StatusService.system
          }
        }
      }
    }

  val geomesaRoutes =
    pathPrefix("geomesa") {
      pathPrefix(Segment) { tableName =>
        pathPrefix("status") {
          get {
            StatusService.geomesa(tableName)
          }
        } ~
        pathPrefix("sfts") {
          pathEndOrSingleSlash {
            get {
              geomesa.SimpleFeatureTypes.list(tableName)
            }
          } ~
          pathPrefix(Segment) { sftName =>
            get {
              geomesa.SimpleFeatureTypes.detail(tableName, sftName)
            }
          }
        }
      }
    }

  val geowaveRoutes =
    pathPrefix("geowave") {
      pathPrefix(Segment) { tableName =>
        pathPrefix("status") {
          get {
            StatusService.geowave(tableName)
          }
        } ~
        pathPrefix("sfts") {
          pathEndOrSingleSlash {
            get {
              geowave.SimpleFeatureTypes.list(tableName)
            }
          } ~
          pathPrefix(Segment) { sftName =>
            get {
              geowave.SimpleFeatureTypes.detail(tableName, sftName)
            }
          }
        }
      }
    }

  def apply() =
    systemRoutes ~ geomesaRoutes ~ geowaveRoutes
}
