package core

import component._

import akka.actor.ActorSelection
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, MediaType, Uri}
import akka.http.scaladsl.model.headers.{Accept, Link, LinkParams, LinkValue}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.reflect.ClassTag

trait CommonDirectives {
    val model: ActorSelection

    implicit val timeout = Timeout(10.seconds)
    import scala.concurrent.ExecutionContext.Implicits.global

    def headComplete = (options | head) { complete("") }

    def jsonLink(uri: String, rel: String, methods: HttpMethod*) = {
        LinkValue(Uri(uri),
            LinkParams.rel(rel),
            LinkParams.`type`(MediaType.customBinary("application", "json",
                MediaType.Compressible, Nil,
                Map("method" -> methods.map(_.name).mkString(" ")))))
    }

    def respondWithLinks(links: LinkValue*) = respondWithHeader(Link(links : _*))

    def mediaTypeVersion(mediaType: MediaType) = headerValuePF {
        case Accept(mediaRanges)
            if mediaRanges.exists(mt => mt.matches(mediaType)) =>
                mediaRanges.find(mt => mt.matches(mediaType)).flatMap( _.params get "version")
    }

    def getList[T: ClassTag](t: Any)(params: Any*)(implicit m: ToEntityMarshaller[Seq[T]]) = get {
        parameters('offset ? 0, 'limit ? 3) { (offset: Int, limit: Int) =>
            { ctx =>
                (model ? ListWithOffset(t, params, offset, limit)) flatMap {
                    case EntityList(slice: Iterable[T @unchecked]) => ctx.complete(slice.toSeq)
                    case _ => ctx.reject()
                }
            }
        }
    }

    def getEntity[T: ClassTag](ids: String*)(implicit m: ToEntityMarshaller[T]) = get {
        { ctx =>
            (model ? GetEntity(ids:_*)) flatMap {
                case Some(entity: T) => ctx.complete(entity)
                case None => ctx.reject()
            }
        }
    }

    def putEntity[T : ClassTag](modify: T => T, ids: String*)
        (implicit u: FromRequestUnmarshaller[T], m: ToEntityMarshaller[T]) = put {
        entity(as[T]) { entity => ctx =>
            (model ? AddEntity(modify(entity), ids:_*)) flatMap {
                case entity: T => ctx.complete(entity)
            }
        }
    }

    def deleteEntity[T: ClassTag](ids: String*)(implicit m: ToEntityMarshaller[T]) = delete {
        { ctx =>
            (model ? DeleteEntity(ids:_*)) flatMap {
                case Some(entity: T) => ctx.complete(entity)
                case None => ctx.reject()
            }
        }
    }
}
// vim: set ts=4 sw=4 et:
