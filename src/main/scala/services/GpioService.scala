package services

import akka.actor.{Actor, ActorContext, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.MalformedFormFieldRejection
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import gpio4s._
import net.codingwell.scalaguice.ScalaModule
import services.api.HealthRoutes._
import services.api.InfoRoutes._
import wiii.awa.WebHooks
import wiii.inject._

object GpioRestService extends WebHooks {
    implicit val actorSystem: ActorSystem = ActorSystem("ServiceB")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    override def config: Option[Config] = Option(ConfigFactory.parseString("webapi.port=2222"))

    val gpio = GpioService(GpioInfo.Pi2b, Inject[PinProducer])

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

trait Pin extends Actor
class MockPin extends Pin {
    def receive: Receive = {
        case m => println(s"mock: $m")
    }
}

class MockPinProducer extends PinProducer {
    def get(num: Int)(implicit ctx: ActorContext): PinRef = InjectActor[Pin]
}

class MockGpioModule extends ScalaModule {
    def configure(): Unit = {
        bind[PinProducer].to[MockPinProducer]
        bind[Pin].to[MockPin]
    }
}

class Pi4jGpioModule extends ScalaModule {
    def configure(): Unit = {
        bind[PinProducer].to[MockPinProducer]
    }
}
