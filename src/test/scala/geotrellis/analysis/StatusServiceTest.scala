package geotrellis.analysis

import akka.http.scaladsl.model.StatusCodes
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.CirceSupport

import geotrellis.analysis.model._

class StatusServiceTest extends ServiceTestBase with CirceSupport {
  "StatusService" when {
    "GET /status/uptime" should {
      "return uptime" in {
        Get("/status/uptime") ~> Routes() ~> check {
          status should be(StatusCodes.OK)
          responseAs[Status].uptime should include("milliseconds")
        }
      }
    }
  }
}
