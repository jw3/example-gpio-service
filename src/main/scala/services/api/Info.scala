package services.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global

case class InfoReport(status: String = "OK")

object InfoProtocol extends DefaultJsonProtocol {
    implicit val info = jsonFormat1(InfoReport)
}

// an info service would make for a good example of Guice utilization
object InfoRoutes {
    import services.api.InfoProtocol._
    val infoRoutes = path("info") {get { r => r.complete(Marshal(InfoReport()).toResponseFor(r.request)) }}
}
