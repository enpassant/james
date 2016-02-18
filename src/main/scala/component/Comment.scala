package component

import core.BaseFormats

import org.json4s.jackson.Serialization.{ read, writePretty }
import org.json4s.{ DefaultFormats, Formats }
import org.joda.time.DateTime
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

case class Comment(id: String = null, blogId: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

trait CommentFormats extends BaseFormats {
    lazy val `application/vnd.comment+json` =
        customMediaTypeUTF8("vnd.comment+json")
    lazy val `application/vnd.comment-v2+json` =
        customMediaTypeUTF8("vnd.comment-v2+json")

    implicit val CommentUnmarshaller = Unmarshaller.firstOf(
        unmarshaller[Comment](`application/vnd.comment-v2+json`),
        unmarshaller[Comment](`application/vnd.comment+json`),
        unmarshaller[Comment](MediaTypes.`application/json`))

    implicit val CommentMarshaller = Marshaller.oneOf(
        marshaller[Comment](`application/vnd.comment-v2+json`),
        marshaller[Comment](`application/vnd.comment+json`),
        marshaller[Comment](MediaTypes.`application/json`))

    implicit val SeqCommentMarshaller = marshaller[Seq[Comment]](
        MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
