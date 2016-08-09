package geotrellis.analysis.geowave

import mil.nga.giat.geowave.adapter.vector.FeatureDataAdapter
import org.opengis.feature.simple._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import geotrellis.analysis._
import connection._

object SimpleFeatureTypes extends BaseService
    with GeoWaveConnection
    with System.LoggerExecutor {
  import scala.collection.JavaConversions._

  def list = complete {
    gwAdapterStore
      .getAdapters()
      .toList.map {
        _.asInstanceOf[FeatureDataAdapter].getType.getTypeName
      }
  }

  def detail(typeName: String) = ???
}

