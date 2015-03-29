package core

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

trait ServiceFormats extends BaseFormats {
    lazy val `application/vnd.enpassant.service+json` =
        MediaTypes.register(MediaType.custom("application/vnd.enpassant.service+json"))

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
