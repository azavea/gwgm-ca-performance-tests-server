package geotrellis.analysis.geowave

import mil.nga.giat.geowave.adapter.vector.FeatureDataAdapter
import org.opengis.feature.simple._
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._

import geotrellis.analysis._
import connection._

object SimpleFeatureTypes extends BaseService
    with GeoWaveConnection
    with AkkaSystem.LoggerExecutor {
  import scala.collection.JavaConversions._

  def list = {
    log.info("/geowave/sfts executed")
    complete {
      gwAdapterStore
        .getAdapters()
        .toList.map {
          _.asInstanceOf[FeatureDataAdapter].getType.getTypeName
        }
    }
  }

  def detail(typeName: String) = ???
}

