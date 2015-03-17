import java.util.UUID
import org.json4s.jackson.Serialization.{ read, writePretty }
import spray.httpx.Json4sSupport
import org.json4s.{ DefaultFormats, Formats }
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import spray.http.{ ContentType, ContentTypeRange, HttpEntity, MediaType, MediaTypes }

case class MicroService(
    uuid: String = UUID.randomUUID.toString,
    path: String,
    host: String,
    port: Int = 9000,
    runningMode: Option[String] = None)

trait ServiceFormats {
    private implicit val formats = DefaultFormats

    lazy val `application/vnd.enpassant.service+json` =
        MediaTypes.register(MediaType.custom("application/vnd.enpassant.service+json"))

    def unmarshal[T](mediaType: ContentTypeRange*)(implicit m: Manifest[T]): Unmarshaller[T] =
        Unmarshaller[T](mediaType:_*) {
            case HttpEntity.NonEmpty(contentType, data) =>
                read[T](data.asString)
        }

    def marshal[T <: AnyRef](contentType: ContentType*): Marshaller[T] =
        Marshaller.of[T](contentType:_*) { (value, contentType, ctx) =>
            ctx.marshalTo(HttpEntity(contentType, writePretty[T](value)))
        }

    implicit val ServiceUnmarshaller = Unmarshaller.oneOf(
        unmarshal[MicroService](`application/vnd.enpassant.service+json`),
        unmarshal[MicroService](MediaTypes.`application/json`))

    implicit val ServiceMarshaller = marshal[MicroService](
        `application/vnd.enpassant.service+json`,
        MediaTypes.`application/json`)

    implicit val SeqServiceMarshaller = marshal[Seq[MicroService]](
        MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
