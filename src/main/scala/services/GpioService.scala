package services

import akka.actor.{Actor, ActorContext, ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.MalformedFormFieldRejection
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import gpio4s._
import services.api.HealthRoutes._
import services.api.InfoRoutes._
import wiii.awa.WebHooks

object GpioRestService extends WebHooks {
    implicit def actorSystem: ActorSystem = ActorSystem("ServiceB")
    implicit def materializer: ActorMaterializer = ActorMaterializer()
    override def config: Option[Config] = Option(ConfigFactory.parseString("webapi.port=2222"))

    val mockpp = new PinProducer {def get(num: Int)(implicit ctx: ActorContext): PinRef = actorSystem.actorOf(Props[MockPin])}

    // DI - inject the appropriate PinProvider
    //val gpio = GpioService(GpioInfo.Pi2b, Pi4jPinProducer())
    val gpio = GpioService(GpioInfo.Pi2b, mockpp)

    val gpioRoutes =
        (put & pathPrefix("set")) {
            path("pin" / IntNumber / Map("off" -> 0, "on" -> 1)) { (p, s) =>
                (p, s) match {
                    case t if vp(t._1) && vs(t._2) =>
                        gpio ! DigitalWrite(t._1, t._2 == 1)
                        complete("!")
                    case t if !vp(t._1) =>
                        reject(MalformedFormFieldRejection("pin", "invalid range"))
                }
            }
        }

    def main(args: Array[String]) {
        webstart(gpioRoutes ~ webhookRoutes ~ healthRoutes ~ infoRoutes)
    }

    def vs(s: Int) = s == 0 || s == 1
    def vp(p: Int) = GpioInfo.Pi2b.pins.contains(p)
}

class MockPin extends Actor {
    def receive: Receive = {
        case m => println(m)
    }
}
