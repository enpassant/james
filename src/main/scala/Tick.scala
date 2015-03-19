import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
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

class TickActor(val config: Config) extends Actor with ActorLogging  with ServiceFormats {
    implicit val system: ActorSystem = ActorSystem("james")
    implicit val timeout: Timeout = Timeout(15.seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    val serviceUri = s"http://${config.serviceHost}:${config.servicePort}/services"
    val microService = MicroService(UUID.randomUUID.toString, "blogs",
        config.host, config.port, config.mode)

    def receive = {
        case Tick =>
            val response: Future[HttpResponse] =
                (IO(Http) ? Put(serviceUri + "/" + microService.uuid, microService))
                    .mapTo[HttpResponse]
            response.map { r => log.info(r.toString) }
    }
}

// vim: set ts=4 sw=4 et:
