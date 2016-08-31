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
    * Perform a timed query.
    *
    * @param query      The function used to query either GeoWave or GeoMesa
    * @param tableName  The name of the GeoMesa table or GeoWave namespace to query
    * @param typeName   The SimpleFeatureTypeName (schema) to query
    * @param where      The name of the field in the SimpleFeatureType that contains spatial data
    * @param xmin       The x-coordinate of the query point, or of the lower-left corner of the query box
    * @param ymin       The y-coordinate of the query point, or of the lower-left corner of the query box
    * @param xmax       The x-coordinate of the upper-right corner of the query box
    * @param ymax       The y-coordinate of the upper-right corner of the query box
    * @param when       The name of the field in the SimpleFeatureType that contans the temporal data
    * @param fromTime   The start time of the temporal range
    * @pram  toTime     The finish time of the temporal range
    */
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
    * Query GeoWave and/or GeoMesa.
    *
    * @param waveQuery The function used for performing GeoWave queries
    * @param mesaQuery The function used for performing GeoMesa queries
    */
  def queryBoth(waveQuery: QueryFn, mesaQuery: QueryFn) =
    pathPrefix("queries") {
      /*
       * The optional parameter "width" gives the width and height of
       * the range query boxes to generate.  If this parameter is not
       * given, then point queries are done.
       *
       * The parameter "n" is the number of queries to perform against
       * GeoMesa and/or against GeoWave.
       *
       * The parameter "seed" is the RNG seed to use for generating
       * random queries.
       *
       * The optional parameter "mesaTable" gives the name of the
       * GeoMesa table (the Accumulo table name prefixes) to query.
       * If this parameter is not given, then GeoMesa is not queried.
       *
       * The optional parameter "waveTable" gives the name of the
       * GeoWave namespace (the Accumulo table name prefixes) to
       * query.  If this parameter is not given, then GeoWave is not
       * queried.
       *
       * The "sftName" parameter gives the SimpleFeatureTypeName
       * (schema) to look for.
       *
       * The parameters "from" and "to" provide the temporal range in
       * which to query.
       */
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
}
