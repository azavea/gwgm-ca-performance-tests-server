package geotrellis.analysis.results

import com.amazonaws.auth.{DefaultAWSCredentialsProviderChain, AWSCredentials, AWSCredentialsProvider}
import com.amazonaws.services.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model._

import java.util.UUID
import scala.collection.JavaConverters._

object DynamoDB {
  val TABLE_NAME = "gwgm-ca-results"
  val TEST = true

  def credentials = new DefaultAWSCredentialsProviderChain()
  lazy val db = new AmazonDynamoDBClient(credentials)

  private def a(s: String): AttributeValue =
    new AttributeValue(s)

  private def a(n: Long): AttributeValue = {
    val a = new AttributeValue
    a.setN(n.toString)
    a
  }

  private def a(n: Int): AttributeValue = {
    val a = new AttributeValue
    a.setN(n.toString)
    a
  }

  private def a(b: Boolean): AttributeValue = {
    val a = new AttributeValue
    a.setBOOL(b)
    a
  }

  // For testing purposes
  // def main(args: Array[String]): Unit = {
  //   val table = db.describeTable(TABLE_NAME)
  //   println(table)

  //   val r =
  //     RunResult(
  //       "test",
  //       Some(
  //         TestResult(
  //           "GeoMesa cluster id",
  //           500L,
  //           600L,
  //           "5 results"
  //         )
  //       ),
  //       Some(
  //         TestResult(
  //           "GeoWave cluster id",
  //           500L,
  //           650L,
  //           "5 results"
  //         )
  //       )
  //     )

  //   saveResult(r)
  // }

  def saveResult(runResult: RunResult): Unit = {
    // Put GM results
    runResult.gmResult.foreach { r =>
      val uuid = UUID.randomUUID.toString

      db.putItem(
        TABLE_NAME,
        Map(
          "uuid" -> a(uuid),
          "testName" -> a(runResult.testName),
          "system" -> a("GM"),
          "clusterId" -> a(r.clusterId),
          "startTime" -> a(r.startTime),
          "endTime" -> a(r.endTime),
          "duration" -> a(r.duration),
          "result" -> a(r.result),
          "test" -> a(TEST)
        ).asJava
      )
    }

    // Put GW results
    runResult.gwResult.foreach { r =>
      val uuid = UUID.randomUUID.toString

      db.putItem(
        TABLE_NAME,
        Map(
          "uuid" -> a(uuid),
          "testName" -> a(runResult.testName),
          "system" -> a("GW"),
          "clusterId" -> a(r.clusterId),
          "startTime" -> a(r.startTime),
          "endTime" -> a(r.endTime),
          "duration" -> a(r.duration),
          "result" -> a(r.result),
          "test" -> a(TEST)
        ).asJava
      )
    }
  }
}
