import akka.actor.ActorRef
import spray.routing.{ HttpServiceActor, Route, ValidationRejection }
import org.joda.time.DateTime
import java.util.UUID

class Service(val config: Config, val model: ActorRef) extends HttpServiceActor
    with BlogFormats with BlogsDirectives {
    import context.dispatcher

    def receive = runRoute {
        log {
            path("") {
                blogLinks { headComplete }
            } ~
            pathPrefix("blogs") {
                handleBlogs ~
                pathPrefix(Segment)(handleBlog)
            }
        }
    }

    def log(route: Route): Route = {
        if (config.mode == Some("dev")) {
            ctx =>
                val start = System.currentTimeMillis
                println(ctx)
                route(ctx)
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
