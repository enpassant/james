import akka.actor.{ActorLogging, ActorRef}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.can.Http
import spray.routing.{ HttpServiceActor, Route, ValidationRejection }
import org.joda.time.DateTime
import java.util.UUID

class Service(val config: Config, val model: ActorRef, tickActor: Option[ActorRef])
    extends HttpServiceActor with BlogFormats with BlogsDirectives with ActorLogging
{
    import context.dispatcher
    implicit val system = context.system

    IO(Http) ! Http.Bind(self, interface = config.host, port = config.port)

    def receive = runRoute {
        log {
            restartTick {
                path("") {
                    blogLinks { headComplete }
                } ~
                pathPrefix("blogs") {
                    handleBlogs ~
                    pathPrefix(Segment)(handleBlog)
                }
            }
        }
    }

    def restartTick(route: Route): Route = { requestContext =>
        tickActor map { _ ! Restart }
        route(requestContext)
    }

    def log(route: Route): Route = {
        if (config.mode == Some("dev")) {
            requestContext =>
                val start = System.currentTimeMillis
                println(requestContext)
                route(requestContext)
                val runningTime = System.currentTimeMillis - start
                println(s"Running time is ${runningTime} ms")
        } else route
    }

    def handleBlogs = (pathEnd compose blogLinks) {
        headComplete ~
        getList[Blog](Blog)
    }

    def handleBlog(blogId: String) = pathEnd {
        (blogLinks & commentLinks) {
            headComplete ~
            getEntity[Blog](blogId) ~
            putEntity[Blog](blogId, _.copy(id = blogId)) ~
            deleteEntity[Blog](blogId)
        }
    }
}
// vim: set ts=4 sw=4 et:
