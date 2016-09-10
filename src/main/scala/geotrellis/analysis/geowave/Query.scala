package geotrellis.analysis.geowave

import geotrellis.analysis._
import geotrellis.analysis.geowave.connection._

import com.vividsolutions.jts.geom._
import com.vividsolutions.jts.geom.GeometryFactory
import mil.nga.giat.geowave.adapter.vector.FeatureDataAdapter
import mil.nga.giat.geowave.core.geotime.store.query._
import mil.nga.giat.geowave.core.store.query.aggregate._
import mil.nga.giat.geowave.core.store.query.{Query => GeoWaveQuery, QueryOptions}
import org.opengis.feature.simple._

import scala.collection.JavaConversions._


object Query {

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  val geometryFactory = new GeometryFactory

  def query(
    gwNamespace: String,
    typeName: String,
    where: String,
    lowerLeft: Seq[Double],
    upperRight: Option[Seq[Double]],
    when: Option[String],
    fromTime: Option[String],
    toTime: Option[String],
    tserverCount: Boolean
  ): Int = {
    /* Get DataStore, DataAdapter */
    val ds = GeoWaveConnection.dataStore(gwNamespace)
    val adapter = GeoWaveConnection.adapterStore(gwNamespace)
      .getAdapters
      .map(_.asInstanceOf[FeatureDataAdapter])
      .filter(_.getType.getTypeName == typeName)
      .next

    /* Create the QueryOptions */
    val queryOptions = new QueryOptions(adapter)
    if (tserverCount) { // https://gitter.im/ngageoint/geowave?at=57c87c20861faa7f07ad31b9
      queryOptions.setAggregation(new CountAggregation, adapter)
    }

    /* Get the Query Geometry, either a box or a point */
    val geom = upperRight match {
      case Some(upperRight) =>
        val envelope = new Envelope(upperRight(0), lowerLeft(0), upperRight(1), lowerLeft(1))
        geometryFactory.toGeometry(envelope)
      case _ =>
        val coordinate = new Coordinate(lowerLeft(0), lowerLeft(1))
        geometryFactory.createPoint(coordinate)
    }

    /* Create the Query */
    val query: GeoWaveQuery = (when, fromTime, toTime) match {
      case (Some(when), Some(fromTime), Some(toTime)) =>
        val start = dateFormat.parse(fromTime)
        val end = dateFormat.parse(toTime)
        new SpatialTemporalQuery(start, end, geom)
      case (Some(when), Some(fromTime), _) =>
        val start = dateFormat.parse(fromTime)
        new SpatialTemporalQuery(start, start, geom)
      case _ =>
        new SpatialQuery(geom)
    }

    val itr = ds.query(queryOptions, query)
    if (tserverCount) {
      var n: Long = 0
      while (itr.hasNext) {
        val countResult = (itr.next).asInstanceOf[CountResult]
        n += countResult.getCount
      }

      itr.close; n.toInt
    }
    else {
      var n = 0
      while (itr.hasNext) {
        val feature: SimpleFeature = itr.next
        // println(s"WAVE ${feature}")
        n += 1
      }
      itr.close; n // return value
    }
  }

}

