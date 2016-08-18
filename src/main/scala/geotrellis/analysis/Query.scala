package geotrellis.analysis

import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future


object Query
    extends BaseService
    with AkkaSystem.LoggerExecutor {

  type QueryFn = (
    String, // tableName or gwNamespace
    String, // SimpleFeatureTypeName
    String, // "where" field in SimpleFeatureType
    Seq[Double], // lower-left point
    Option[Seq[Double]], // upper-right point
    Option[String], // "when" field in SimpleFeatureType
    Option[String], // start time
    Option[String] // end time
  ) => Int

  /**
    * Perform a spatial range query, return the amount of time and the
    * number of records.
    */
  def spatialRangeQuery(
    query: QueryFn,
    tableName: String, typeName: String,
    where: String, xmin: Double, ymin: Double, xmax: Double, ymax: Double
  ) = complete {
    Future {
      val before = System.currentTimeMillis
      val n =
        query(
          tableName, typeName,
          where, List(xmin, ymin), Some(List(xmax, ymax)),
          None, None, None
        )
      val after = System.currentTimeMillis

      Map[String, Long](
        "time" -> (after - before),
        "results" -> n
      )
    }
  }

  /**
    * Perform a spatio-temporal range query, return the amount of time
    * and the number of records.
    */
  def spatioTemporalRangeQuery(
    query: QueryFn,
    tableName: String, typeName: String,
    where: String, xmin: Double, ymin: Double, xmax: Double, ymax: Double,
    when: String, fromTime: String, toTime: String
  ) = complete {
    val before = System.currentTimeMillis
    val n =
      query(
        tableName, typeName,
        where, List(xmin, ymin), Some(List(xmax, ymax)),
        Some(when), Some(fromTime), Some(toTime)
      )
    val after = System.currentTimeMillis

    Map[String, Long](
      "time" -> (after - before),
      "results" -> n
    )
  }

  def rangeQuery(query: QueryFn, tableName: String) =
    pathPrefix("rangequery") {
      parameters('xmin, 'ymin, 'xmax, 'ymax, 'from ?, 'to ?) { (_xmin, _ymin, _xmax, _ymax, _from, _to) =>
        val xmin = _xmin.toDouble
        val ymin = _ymin.toDouble
        val xmax = _xmax.toDouble
        val ymax = _ymax.toDouble

        (_from, _to) match {
          case (Some(from), Some(to)) =>
            spatioTemporalRangeQuery(
              query,
              tableName,
              "CommonPointSimpleFeatureType",
              "where", xmin, ymin, xmax, ymax,
              "when", from, to
            )
          case _ =>
            spatialRangeQuery(
              query,
              tableName,
              "CommonPointSimpleFeatureType",
              "where", xmin, ymin, xmax, ymax
            )
        }
      }
    }
}
