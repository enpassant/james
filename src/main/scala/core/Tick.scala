package core

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, RequestEntity}
import akka.http.scaladsl.model.HttpMethods._
import akka.stream.ActorMaterializer

case class Tick()
case class Restart()

class TickActor(val config: Config) extends Actor with ActorLogging  with ServiceFormats {
    implicit val timeout: Timeout = Timeout(15.seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    val serviceUri = s"http://${config.router.get}/services"
    val microService = MicroService(UUID.randomUUID.toString, "blogs",
        config.host, config.port, config.mode)
    val entity = Marshal(microService).to[RequestEntity]

    implicit val system = context.system
    implicit val materializer = ActorMaterializer()

    def register() = {
        val response: Future[HttpResponse] = entity.flatMap { e =>
            Http().singleRequest(HttpRequest(method = PUT,
                uri = serviceUri + "/" + microService.uuid,
                entity = e))
        }
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
    def props(config: Config) = Props(new TickActor(config))
    def name = "tick"
}
// vim: set ts=4 sw=4 et:

