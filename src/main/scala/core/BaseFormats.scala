package core

import org.json4s.jackson.Serialization.{ read, writePretty }
import org.json4s.{ DefaultFormats, Formats, jackson, Serialization }
import org.joda.time.DateTime
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.model.{ ContentType, ContentTypeRange, HttpEntity, MediaType, MediaTypes }
import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ HttpCharsets, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }

object BaseFormats extends BaseFormats {

  sealed abstract class ShouldWritePretty

  object ShouldWritePretty {
    object True extends ShouldWritePretty
    object False extends ShouldWritePretty
  }
}

trait BaseFormats {
  import BaseFormats._

  implicit val serialization = jackson.Serialization
  implicit val formats = DefaultFormats ++ org.json4s.ext.JodaTimeSerializers.all

  def customMediaTypeUTF8(name: String): MediaType.WithFixedCharset =
      MediaType.customWithFixedCharset(
          "application",
          name,
          HttpCharsets.`UTF-8`
      )

  implicit def json4sUnmarshallerMediaType[A: Manifest](mediaType: MediaType)
    (serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    unmarshaller(mediaType)(manifest, serialization, formats)

  implicit def json4sUnmarshallerConverter[A: Manifest]
    (implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    unmarshaller(MediaTypes.`application/json`)(manifest, serialization, formats)

  /**
   * HTTP entity => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def unmarshaller[A: Manifest](mediaType: MediaType)
    (implicit serialization: Serialization, formats: Formats): FromEntityUnmarshaller[A] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(mediaType)
      .mapWithCharset { (data, charset) =>
        val input = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
        serialization.read(input)
      }

  implicit def json4sMarshallMediaType[A <: AnyRef](mediaType: MediaType)
    (serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    marshaller(mediaType)(serialization, formats, shouldWritePretty)

  //implicit def json4sMarshallerConverter[A <: AnyRef]
    //(implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    //marshaller(MediaTypes.`application/json`)(serialization, formats, shouldWritePretty)

  /**
   * `A` => HTTP entity
   *
   * @tparam A type to encode, must be upper bounded by `AnyRef`
   * @return marshaller for any `A` value
   */
  implicit def marshaller[A <: AnyRef](mediaType: MediaType)
    (implicit serialization: Serialization, formats: Formats, shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    shouldWritePretty match {
      case ShouldWritePretty.False => Marshaller.StringMarshaller.wrap(mediaType)(serialization.write[A])
      case _                       => Marshaller.StringMarshaller.wrap(mediaType)(serialization.writePretty[A])
    }
}

