package geotrellis.analysis

import akka.http.scaladsl.server.Directives._
import scala.concurrent.Future


object Query
    extends BaseService
    with AkkaSystem.LoggerExecutor {

  val rng = new scala.util.Random

  type QueryFn = (
    String,              // tableName or gwNamespace
    String,              // SimpleFeatureTypeName
    String,              // "where" field in SimpleFeatureType
    Seq[Double],         // lower-left point
    Option[Seq[Double]], // upper-right point
    Option[String],      // "when" field in SimpleFeatureType
    Option[String],      // start time
    Option[String]       // end time
  ) => Int


  /**
    * Perform a spatial range query, return the amount of time and the
    * number of records.
    */
  def spatialRangeQuery(
    query: QueryFn,
    tableName: String, typeName: String,
    where: String, xmin: Double, ymin: Double, xmax: Double, ymax: Double
  ) = {
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

  /**
    * Perform a spatio-temporal range query, return the amount of time
    * and the number of records.
    */
  def spatioTemporalRangeQuery(
    query: QueryFn,
    tableName: String, typeName: String,
    where: String, xmin: Double, ymin: Double, xmax: Double, ymax: Double,
    when: String, fromTime: String, toTime: String
  ) = {
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

  /**
    * Query both GeoWave and GeoMesa simultaneously.
    */
  def queryBoth(waveQuery: QueryFn, mesaQuery: QueryFn) =
    pathPrefix("rangequeries") {
      parameters('width, 'n, 'seed, 'mesaTable ?, 'waveTable ?, 'sftName, 'from ?, 'to ?) {
        (width, n, seed, mesaTable, waveTable, sftName, _from, _to) =>
        rng.setSeed(seed.toLong)
        complete {
          Future {
            (0 until n.toInt)
              .map({ i =>
                val xmin = math.max(-180, 360*rng.nextDouble - 180)
                val ymin = math.max(-90,  180*rng.nextDouble - 90)
                val xmax = math.min(180,  xmin + width.toDouble)
                val ymax = math.min(90,   ymin + width.toDouble)

                val info = Map[String, Long](
                  // "xmin" -> (xmin * 1000000).toLong,
                  // "ymin" -> (ymin * 1000000).toLong,
                  // "xmax" -> (xmax * 1000000).toLong,
                  // "ymax" -> (ymax * 1000000).toLong,
                  "i" -> i
                )

                val wave = (_from, _to, waveTable) match {
                  case (Some(from), Some(to), Some(waveTable)) =>
                    Some(spatioTemporalRangeQuery(
                      waveQuery, waveTable, sftName,
                      "where", xmin, ymin, xmax, ymax,
                      "when", from, to))
                  case (_, _, Some(waveTable)) =>
                    Some(spatialRangeQuery(
                      waveQuery, waveTable, sftName,
                      "where", xmin, ymin, xmax, ymax))
                  case _ => None
                }

                val mesa = (_from, _to, mesaTable) match {
                  case (Some(from), Some(to), Some(mesaTable)) =>
                    Some(spatioTemporalRangeQuery(
                      mesaQuery, mesaTable, sftName,
                      "where", xmin, ymin, xmax, ymax,
                      "when", from, to))
                  case (_, _, Some(mesaTable)) =>
                    Some(spatialRangeQuery(
                      mesaQuery, mesaTable, sftName,
                      "where", xmin, ymin, xmax, ymax))
                  case _ => None
                }

                (mesa, wave) match {
                  case (Some(mesa), Some(wave)) =>
                    Map("info" -> info, "mesa" -> mesa, "wave" -> wave)
                  case (None, Some(wave)) =>
                    Map("info" -> info, "wave" -> wave)
                  case (Some(mesa), None) =>
                    Map("info" -> info, "mesa" -> mesa)
                  case _ => throw new Exception
                }
              })
          }
        }
      }
    }

  def rangeQuery(query: QueryFn, tableName: String) =
    pathPrefix(Segment) { sftName =>
      pathPrefix("rangequery") {
        parameters('xmin, 'ymin, 'xmax, 'ymax, 'from ?, 'to ?) { (_xmin, _ymin, _xmax, _ymax, _from, _to) =>
          val xmin = _xmin.toDouble
          val ymin = _ymin.toDouble
          val xmax = _xmax.toDouble
          val ymax = _ymax.toDouble

          (_from, _to) match {
            case (Some(from), Some(to)) =>
              complete {
                Future {
                  spatioTemporalRangeQuery(
                    query,
                    tableName,
                    sftName,
                    "where", xmin, ymin, xmax, ymax,
                    "when", from, to
                  )
                }
              }

            case _ =>
              complete {
                Future {
                  spatialRangeQuery(
                    query,
                    tableName,
                    sftName,
                    "where", xmin, ymin, xmax, ymax
                  )
                }
              }
          }
        }
      }
    }
}
