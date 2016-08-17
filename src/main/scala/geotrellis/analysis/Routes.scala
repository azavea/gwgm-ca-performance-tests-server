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
        } ~
        pathPrefix("rangequery") {
          parameters('xmin, 'ymin, 'xmax, 'ymax, 'from ?, 'to ?) { (_xmin, _ymin, _xmax, _ymax, _to, _from) =>
            val xmin = _xmin.toDouble
            val ymin = _ymin.toDouble
            val xmax = _xmax.toDouble
            val ymax = _ymax.toDouble

            (_from, _to) match {
              case (Some(from), Some(to)) =>
                geomesa.Query.spatioTemporalRangeQuery(
                  tableName,
                  "CommonPointSimpleFeatureType",
                  "where", xmin, ymin, xmax, ymax,
                  "when", from, to
                )
              case _ =>
                geomesa.Query.spatialRangeQuery(
                  tableName,
                  "CommonPointSimpleFeatureType",
                  "where", xmin, ymin, xmax, ymax
                )
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
