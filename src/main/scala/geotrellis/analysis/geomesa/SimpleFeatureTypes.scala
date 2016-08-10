package geotrellis.analysis.geomesa

import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import geotrellis.analysis._
import connection._

object SimpleFeatureTypes extends BaseService
    with GeoMesaConnection
    with System.LoggerExecutor {
  import scala.collection.JavaConversions._

  def list = complete(gmDataStore.getTypeNames)

  def detail(typeName: String) = complete {
    gmDataStore.getSchema(typeName)
      .getAttributeDescriptors
      .map { attr => attr.getLocalName() }
  }
}

