import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing.{ HttpServiceActor, ValidationRejection }
import org.joda.time.DateTime
import java.util.UUID

class Service(model: ActorRef) extends HttpServiceActor with BlogFormats with BlogsDirectives {
    import context.dispatcher

    implicit val timeout = Timeout(10.seconds)

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
        get {
            parameters('offset ? 0, 'limit ? 3) { (offset: Int, limit: Int) =>
                completeJson {
                    (model ? ListWithOffset(Blog, offset, limit)) map {
                        case Blogs(slice) => slice.toSeq
                    }
                }
            }
        }
    }

    def handleBlog(blogId: String) = (blogLinks & commentLinks) {
        headComplete ~
        put {
            entity(as[Blog]) { blog => ctx =>
                (model ? AddBlog(blog.copy(id=blogId))) map {
                    case blog: Blog => ctx.complete(blog)
                }
            }
        } ~
        get {
            respondWithJson {
                ctx => (model ? blogId) map {
                    case Some(blog: Blog) => ctx.complete(blog)
                    case None => ctx.reject()
                }
            }
        }
    }
}
// vim: set ts=4 sw=4 et:
