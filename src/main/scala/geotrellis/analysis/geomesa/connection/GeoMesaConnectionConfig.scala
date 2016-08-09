package geotrellis.analysis.geomesa.connection

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus
import net.ceedubs.ficus.readers.ArbitraryTypeReader

import org.geotools.data.DataStoreFinder
import org.locationtech.geomesa.accumulo.data.AccumuloDataStore

case class GeoMesaConnectionConfig(
    user: String,
    password: String,
    instance: String,
    zookeepers: String,
    table: String
) {
  def toMap = Map(
    "user" -> this.user,
    "password" -> this.password,
    "instance" -> this.instance,
    "zookeepers" -> this.zookeepers,
    "table" -> this.table
  )
}
