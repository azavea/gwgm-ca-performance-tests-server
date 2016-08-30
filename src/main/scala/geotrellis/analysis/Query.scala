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


  def timedQuery(
    query: QueryFn,
    tableName: String, typeName: String,
    where: String, xmin: Double, ymin: Double, xmax: Option[Double], ymax: Option[Double],
    when: Option[String] = None, fromTime: Option[String] = None, toTime: Option[String] = None
  ) = {
    val before = System.currentTimeMillis

    val n =
      (xmax, ymax) match {
        case (Some(xmax), Some(ymax)) =>
          query(
            tableName, typeName,
            where, List(xmin, ymin), Some(List(xmax, ymax)),
            when, fromTime, toTime
          )
        case _ =>
          query(
            tableName, typeName,
            where, List(xmin, ymin), None,
            when, fromTime, toTime
          )
      }

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
      parameters('width ?, 'n, 'seed, 'mesaTable ?, 'waveTable ?, 'sftName, 'from ?, 'to ?) {
        (width, n, seed, mesaTable, waveTable, sftName, fromTime, toTime) =>
        rng.setSeed(seed.toLong)
        complete {
          Future {
            (0 until n.toInt)
              .map({ i =>
                val xmin = math.max(-180, 360*rng.nextDouble - 180)
                val ymin = math.max(-90,  180*rng.nextDouble - 90)
                val xmax = width.map({ width => math.min(180, xmin + width.toDouble) })
                val ymax = width.map({ width => math.min(90,  ymin + width.toDouble) })

                val info = Map[String, Long](
                  "i" -> i
                )

                val wave = (fromTime, toTime, waveTable) match {
                  case (Some(from), Some(to), Some(waveTable)) =>
                    Some(timedQuery(
                      waveQuery, waveTable, sftName,
                      "where", xmin, ymin, xmax, ymax,
                      Some("when"), Some(from), Some(to)))
                  case (_, _, Some(waveTable)) =>
                    Some(timedQuery(
                      waveQuery, waveTable, sftName,
                      "where", xmin, ymin, xmax, ymax))
                  case _ => None
                }

                val mesa = (fromTime, toTime, mesaTable) match {
                  case (Some(from), Some(to), Some(mesaTable)) =>
                    Some(timedQuery(
                      mesaQuery, mesaTable, sftName,
                      "where", xmin, ymin, xmax, ymax,
                      Some("when"), Some(from), Some(to)))
                  case (_, _, Some(mesaTable)) =>
                    Some(timedQuery(
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
        parameters('xmin, 'ymin, 'xmax, 'ymax, 'from ?, 'to ?) { (_xmin, _ymin, _xmax, _ymax, fromTime, toTime) =>
          val xmin = _xmin.toDouble
          val ymin = _ymin.toDouble
          val xmax = _xmax.toDouble
          val ymax = _ymax.toDouble

          (fromTime, toTime) match {
            case (Some(from), Some(to)) =>
              complete {
                Future {
                  timedQuery(
                    query,
                    tableName,
                    sftName,
                    "where", xmin, ymin, Some(xmax), Some(ymax),
                    Some("when"), Some(from), Some(to)
                  )
                }
              }

            case _ =>
              complete {
                Future {
                  timedQuery(
                    query,
                    tableName,
                    sftName,
                    "where", xmin, ymin, Some(xmax), Some(ymax)
                  )
                }
              }
          }
        }
      }
    }
}
