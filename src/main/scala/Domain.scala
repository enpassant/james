import org.json4s.jackson.Serialization.{ read, writePretty }
import spray.httpx.Json4sSupport
import org.json4s.{ DefaultFormats, Formats }
import org.joda.time.DateTime
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import spray.http.{ ContentType, ContentTypeRange, HttpEntity, MediaType, MediaTypes }

case class Blog(id: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

case class Comment(id: String = null, blogId: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

trait BlogFormats {
    private implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

    lazy val `application/vnd.blog+json` =
        MediaTypes.register(MediaType.custom("application/vnd.blog+json"))
    lazy val `application/vnd.blog-v1+json` =
        MediaTypes.register(MediaType.custom("application/vnd.blog-v1+json"))

    lazy val `application/vnd.comment+json` =
        MediaTypes.register(MediaType.custom("application/vnd.comment+json"))
    lazy val `application/vnd.comment-v1+json` =
        MediaTypes.register(MediaType.custom("application/vnd.comment-v1+json"))

    def unmarshal[T](mediaType: ContentTypeRange*)(implicit m: Manifest[T]): Unmarshaller[T] =
        Unmarshaller[T](mediaType:_*) {
            case HttpEntity.NonEmpty(contentType, data) =>
                read[T](data.asString)
        }

    def marshal[T <: AnyRef](contentType: ContentType*): Marshaller[T] =
        Marshaller.of[T](contentType:_*) { (value, contentType, ctx) =>
            ctx.marshalTo(HttpEntity(contentType, writePretty[T](value)))
        }

    implicit val BlogUnmarshaller = Unmarshaller.oneOf(
        unmarshal[Blog](`application/vnd.blog-v1+json`),
        unmarshal[Blog](`application/vnd.blog+json`),
        unmarshal[Blog](MediaTypes.`application/json`))

    implicit val BlogMarshaller = marshal[Blog](
        `application/vnd.blog-v1+json`,
        `application/vnd.blog+json`,
        MediaTypes.`application/json`)

    implicit val SeqBlogMarshaller = marshal[Seq[Blog]](
        MediaTypes.`application/json`)

    implicit val CommentUnmarshaller = Unmarshaller.oneOf(
        unmarshal[Comment](`application/vnd.comment-v1+json`),
        unmarshal[Comment](`application/vnd.comment+json`),
        unmarshal[Comment](MediaTypes.`application/json`))

    implicit val CommentMarshaller = marshal[Comment](
        `application/vnd.comment-v1+json`,
        `application/vnd.comment+json`,
        MediaTypes.`application/json`)

    implicit val SeqCommentMarshaller = marshal[Seq[Comment]](
        MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
