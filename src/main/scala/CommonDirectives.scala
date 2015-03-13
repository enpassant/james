import spray.routing.HttpService
import spray.http.{ HttpMethod, MediaType, MediaTypes, Uri }
import spray.http.HttpHeaders._
import spray.httpx.marshalling.ToResponseMarshallable

trait CommonDirectives extends HttpService {
    def respondWithJson =
        respondWithMediaType(MediaTypes.`application/json`)

    def completeJson(block: ToResponseMarshallable) =
        respondWithMediaType(MediaTypes.`application/json`) {
            complete(block)
        }

    def headComplete = (options | head) { complete("") }

    def jsonLink(uri: String, rel: String, methods: HttpMethod*) = {
        Link.Value(Uri(uri),
            Link.rel(rel),
            Link.`type`(MediaType.custom("application", "json",
                parameters = Map("method" -> methods.mkString(" ")))))
    }

    def respondWithLinks(links: Link.Value*) = respondWithHeader(Link(links))
}
// vim: set ts=4 sw=4 et:
