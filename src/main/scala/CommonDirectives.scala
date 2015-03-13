import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing.HttpService
import spray.http.{ HttpMethod, MediaType, MediaTypes, Uri }
import spray.http.HttpHeaders._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import scala.reflect.ClassTag

trait CommonDirectives extends HttpService {
    val model: ActorRef

    implicit val timeout = Timeout(10.seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    def respondWithJson =
        respondWithMediaType(MediaTypes.`application/json`)

    def completeJson(block: ToResponseMarshallable) =
        respondWithMediaType(MediaTypes.`application/json`) {
            complete(block)
        }

    def headComplete = (options | head) { complete("") }

    def jsonLink(uri: String, rel: String, methods: HttpMethod*) = {
        Link.Value(Uri(uri),
            Link.rel(rel),
            Link.`type`(MediaType.custom("application", "json",
                parameters = Map("method" -> methods.mkString(" ")))))
    }

    def respondWithLinks(links: Link.Value*) = respondWithHeader(Link(links))

    def getList[T: ClassTag](t: Any)(implicit m: ToResponseMarshaller[Seq[T]]) = get {
        parameters('offset ? 0, 'limit ? 3) { (offset: Int, limit: Int) =>
            respondWithJson { ctx =>
                (model ? ListWithOffset(t, offset, limit)) map {
                    case EntityList(slice: Iterable[T]) => ctx.complete(slice.toSeq)
                    case _ => ctx.reject()
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
