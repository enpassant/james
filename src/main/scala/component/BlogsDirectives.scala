package component

import core._

import akka.http.scaladsl.model.HttpMethods._

trait BlogsDirectives extends CommonDirectives {
    def blogLinks = respondWithLinks(jsonLink("/blogs", "blogs", GET),
        jsonLink("/blogs/{blogId}", "blog", GET, PUT, DELETE))

    def commentLinks = respondWithLinks(jsonLink("/blogs/{blogId}/comments", "comments", GET),
        jsonLink("/blogs/{blogId}/comments/{commentId}", "comment", GET, PUT, DELETE))
}
// vim: set ts=4 sw=4 et:
