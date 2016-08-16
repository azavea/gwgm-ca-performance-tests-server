package geotrellis.analysis.geowave.connection

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus
import net.ceedubs.ficus.readers.ArbitraryTypeReader

import mil.nga.giat.geowave.datastore.accumulo.operations.config._
import mil.nga.giat.geowave.datastore.accumulo.metadata._
import mil.nga.giat.geowave.datastore.accumulo._

object GeoWaveConnection {
  import Ficus._
  import ArbitraryTypeReader._
  import scala.collection.JavaConversions._

  private val config = ConfigFactory.load()
  protected val geowaveConfig = config.as[GeoWaveConnectionConfig]("geowave")

  private val additionalAccumuloOpts = new AccumuloOptions
  additionalAccumuloOpts.setUseAltIndex(true)

  def accumuloOpts(tableName: String) = {
    val opts = new AccumuloRequiredOptions
    opts.setZookeeper(geowaveConfig.zookeepers)
    opts.setInstance(geowaveConfig.instance)
    opts.setUser(geowaveConfig.user)
    opts.setPassword(geowaveConfig.password)
    opts.setGeowaveNamespace(tableName)
    opts.setAdditionalOptions(additionalAccumuloOpts)
  }

  def basicOperations(tableName: String) =
    new BasicAccumuloOperations(
      geowaveConfig.zookeepers,
      geowaveConfig.instance,
      geowaveConfig.user,
      geowaveConfig.password,
      tableName
    )

  def dataStore(tableName: String) =
    new AccumuloDataStore(basicOperations(tableName))

  def adapterStore(tableName: String) =
    new AccumuloAdapterStore(basicOperations(tableName))
}
