import akka.actor.ActorRef
import spray.routing.{ HttpServiceActor, ValidationRejection }
import org.joda.time.DateTime
import java.util.UUID

class Service(val model: ActorRef) extends HttpServiceActor with BlogFormats with BlogsDirectives {
    import context.dispatcher

    def receive = runRoute {
        path("") {
            blogLinks { headComplete }
        } ~
        pathPrefix("blogs") {
            handleBlogs ~
            path(Segment)(handleBlog)
        }
    }

    def handleBlogs = (pathEnd compose blogLinks) {
        headComplete ~
        getList[Blog](Blog)
    }

    def handleBlog(blogId: String) = (blogLinks & commentLinks) {
        headComplete ~
        getEntity[Blog](blogId) ~
        putEntity[Blog](blogId, _.copy(id=blogId)) ~
        deleteEntity[Blog](blogId)
    }
}
// vim: set ts=4 sw=4 et:
