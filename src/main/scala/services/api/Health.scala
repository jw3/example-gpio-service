package services.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.server.Directives._
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global

case class HealthOK(status: String = "OK")

object HealthProtocol extends DefaultJsonProtocol {
    implicit val healthOk = jsonFormat1(HealthOK)
}

object HealthRoutes {
    import services.api.HealthProtocol._
    val healthRoutes = path("health") {get { r => r.complete(Marshal(HealthOK()).toResponseFor(r.request)) }}
}
