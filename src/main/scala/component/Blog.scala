package component

import core.BaseFormats

import org.json4s.jackson.Serialization.{ read, writePretty }
import org.json4s.{ DefaultFormats, Formats }
import org.joda.time.DateTime
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

case class Blog(id: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

trait BlogFormats extends BaseFormats {
    lazy val `application/vnd.blog+json` =
        customMediaTypeUTF8("vnd.blog+json")
    lazy val `application/vnd.blog-v2+json` =
        customMediaTypeUTF8("vnd.blog-v2+json")

    implicit val BlogUnmarshaller = Unmarshaller.firstOf(
        unmarshaller[Blog](`application/vnd.blog-v2+json`),
        unmarshaller[Blog](`application/vnd.blog+json`),
        unmarshaller[Blog](MediaTypes.`application/json`))

    implicit val BlogMarshaller = Marshaller.oneOf(
        marshaller[Blog](`application/vnd.blog-v2+json`),
        marshaller[Blog](`application/vnd.blog+json`),
        marshaller[Blog](MediaTypes.`application/json`))

    implicit val SeqBlogMarshaller = marshaller[Seq[Blog]](
        MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
