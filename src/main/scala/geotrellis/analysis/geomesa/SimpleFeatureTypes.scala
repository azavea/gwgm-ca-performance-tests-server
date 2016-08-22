package geotrellis.analysis.geomesa

import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import geotrellis.analysis._
import connection._

import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.accumulo.data.AccumuloDataStore

object SimpleFeatureTypes extends BaseService with AkkaSystem.LoggerExecutor {
  import scala.collection.JavaConversions._

  def list(tableName: String) = complete {
    val ds = GeoMesaConnection.dataStore(tableName)
    ds.getTypeNames().toList
  }

  def detail(tableName: String, typeName: String) = complete {
    val ds = GeoMesaConnection.dataStore(tableName)
    ds.getSchema(typeName)
      .getAttributeDescriptors
      .map { attr => attr.getLocalName() }
  }
}

