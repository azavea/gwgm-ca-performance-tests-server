package geotrellis.analysis

import akka.http.scaladsl.server.Directives._

import geotrellis.analysis.status._
import geotrellis.analysis.geomesa.connection._
import geotrellis.analysis.geowave.connection._

object Routes {

  val queryRoutes =
    Query.queryBoth(geowave.Query.query, geomesa.Query.query)

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
        Query.rangeQuery(geomesa.Query.query, tableName)
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
        } ~
        Query.rangeQuery(geowave.Query.query, tableName)
      }
    }

  def apply() =
    systemRoutes ~ geomesaRoutes ~ geowaveRoutes ~ queryRoutes
}
