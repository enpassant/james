package core

import java.util.UUID
import org.json4s.jackson.Serialization.{ read, writePretty }
import org.json4s.{ DefaultFormats, Formats }
import akka.http.scaladsl.model._
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

case class MicroService(
    uuid: String = UUID.randomUUID.toString,
    path: String,
    host: String,
    port: Int = 9000,
    runningMode: Option[String] = None)

trait ServiceFormats extends BaseFormats {
    lazy val `application/vnd.enpassant.service+json` =
        customMediaTypeUTF8("vnd.enpassant.service+json")

    implicit val ServiceUnmarshaller = Unmarshaller.firstOf(
        unmarshaller[MicroService](`application/vnd.enpassant.service+json`),
        unmarshaller[MicroService](MediaTypes.`application/json`))

    implicit val ServiceMarshaller = Marshaller.oneOf(
        marshaller[MicroService](`application/vnd.enpassant.service+json`),
        marshaller[MicroService](MediaTypes.`application/json`))

    implicit val SeqServiceMarshaller = marshaller[Seq[MicroService]](
        MediaTypes.`application/json`)
}
// vim: set ts=4 sw=4 et:
