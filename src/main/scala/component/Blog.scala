package component

import core.BaseFormats

import org.json4s.jackson.Serialization.{ read, writePretty }
import org.json4s.{ DefaultFormats, Formats }
import org.joda.time.DateTime

case class Blog(id: String = null, accountId: String,
    date: DateTime = DateTime.now, title: String, note: String)

trait BlogFormats extends BaseFormats {
    //lazy val `application/vnd.blog+json` =
        //MediaTypes.register(MediaType.custom("application/vnd.blog+json"))
    //lazy val `application/vnd.blog-v1+json` =
        //MediaTypes.register(MediaType.custom("application/vnd.blog-v1+json"))

    //implicit val BlogUnmarshaller = Unmarshaller.oneOf(
        //unmarshal[Blog](`application/vnd.blog-v1+json`),
        //unmarshal[Blog](`application/vnd.blog+json`),
        //unmarshal[Blog](MediaTypes.`application/json`))

    //implicit val BlogMarshaller = marshal[Blog](
        //`application/vnd.blog-v1+json`,
        //`application/vnd.blog+json`,
        //MediaTypes.`application/json`)

    //implicit val SeqBlogMarshaller = marshal[Seq[Blog]](
        //MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
