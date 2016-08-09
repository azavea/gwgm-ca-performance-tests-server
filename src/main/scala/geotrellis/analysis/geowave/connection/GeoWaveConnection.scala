package geotrellis.analysis.geowave.connection

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus
import net.ceedubs.ficus.readers.ArbitraryTypeReader

import mil.nga.giat.geowave.datastore.accumulo.operations.config._
import mil.nga.giat.geowave.datastore.accumulo._

trait GeoWaveConnection {
  import Ficus._
  import ArbitraryTypeReader._
  import scala.collection.JavaConversions._

  private val config = ConfigFactory.load()
  protected val geowaveConfig = config.as[GeoWaveConnectionConfig]("geowave")

  private val additionalAccumuloOpts = new AccumuloOptions
  additionalAccumuloOpts.setUseAltIndex(true)

  private val accumuloOpts = new AccumuloRequiredOptions
  accumuloOpts.setZookeeper(geowaveConfig.zookeepers)
  accumuloOpts.setInstance(geowaveConfig.instance)
  accumuloOpts.setUser(geowaveConfig.user)
  accumuloOpts.setPassword(geowaveConfig.password)
  accumuloOpts.setGeowaveNamespace(geowaveConfig.table)
  accumuloOpts.setAdditionalOptions(additionalAccumuloOpts)

  private val bao = new BasicAccumuloOperations(
    geowaveConfig.zookeepers,
    geowaveConfig.instance,
    geowaveConfig.user,
    geowaveConfig.password,
    geowaveConfig.table
  )

  val gwDataStore = new AccumuloDataStore(bao)
}
