package component

import core.BaseFormats

import org.json4s.jackson.Serialization.{ read, writePretty }
import spray.httpx.Json4sSupport
import org.json4s.{ DefaultFormats, Formats }
import org.joda.time.DateTime
import spray.httpx.marshalling._
import spray.httpx.unmarshalling._
import spray.http.{ ContentType, ContentTypeRange, HttpEntity, MediaType, MediaTypes }

case class Comment(id: String = null, blogId: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

trait CommentFormats extends BaseFormats {
    private implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

    lazy val `application/vnd.comment+json` =
        MediaTypes.register(MediaType.custom("application/vnd.comment+json"))
    lazy val `application/vnd.comment-v1+json` =
        MediaTypes.register(MediaType.custom("application/vnd.comment-v1+json"))

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
