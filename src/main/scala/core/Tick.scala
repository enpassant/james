package core

import component._

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.can.Http
import spray.http._
import HttpMethods._
import spray.httpx.RequestBuilding._

case class Tick()
case class Restart()

class TickActor(val config: Config) extends Actor with ActorLogging  with ServiceFormats {
    implicit val system: ActorSystem = ActorSystem("james")
    implicit val timeout: Timeout = Timeout(15.seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    val serviceUri = s"http://${config.serviceHost.get}:${config.servicePort}/services"
    val microService = MicroService(UUID.randomUUID.toString, "blogs",
        config.host, config.port, config.mode)

    def register() = {
        val response: Future[HttpResponse] =
            (IO(Http) ? Put(serviceUri + "/" + microService.uuid, microService))
                .mapTo[HttpResponse]
        response.map { r => log.debug(r.toString) }
        schedule
    }

    def schedule() = {
        val c = context.system.scheduler.scheduleOnce(60 seconds, self, Tick)
        context.become(process(c))
    }

    def receive = {
        case Tick =>
            register
    }

    def process(cancellable: Cancellable): Receive = {
        case Tick =>
            register

        case Restart =>
            cancellable.cancel
            schedule
    }
}

object TickActor {
}
// vim: set ts=4 sw=4 et:
