import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing.{ HttpServiceActor, ValidationRejection }
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import org.joda.time.DateTime
import java.util.UUID
import scala.reflect.ClassTag

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
        getList(Blog)
    }

    def handleBlog(blogId: String) = (blogLinks & commentLinks) {
        headComplete ~
        getEntity[Blog](blogId) ~
        putEntity[Blog](blogId, _.copy(id=blogId)) ~
        deleteEntity[Blog](blogId)
    }

    def getList(t: Any) = get {
        parameters('offset ? 0, 'limit ? 3) { (offset: Int, limit: Int) =>
            completeJson {
                (model ? ListWithOffset(t, offset, limit)) map {
                    case EntityList(slice) => slice.toSeq
                }
            }
        }
    }

    def getEntity[T: ClassTag](id: String)(implicit m: ToResponseMarshaller[T]) = get {
        respondWithJson { ctx =>
            (model ? id) map {
                case Some(entity: T) => ctx.complete(entity)
                case None => ctx.reject()
            }
        }
    }

    def putEntity[T : ClassTag](id: String, modify: T => T)
        (implicit u: FromRequestUnmarshaller[T], m: ToResponseMarshaller[T]) = put {
        entity(as[T]) { entity => ctx =>
            (model ? AddEntity(modify(entity))) map {
                case entity: T => ctx.complete(entity)
            }
        }
    }

    def deleteEntity[T: ClassTag](id: String)(implicit m: ToResponseMarshaller[T]) = delete {
        respondWithJson { ctx =>
            (model ? DeleteEntity(id)) map {
                case Some(entity: T) => ctx.complete(entity)
                case None => ctx.reject()
            }
        }
    }
}
// vim: set ts=4 sw=4 et:
