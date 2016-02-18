package component

import core._

import akka.http.scaladsl.model.HttpMethods._

trait BlogsDirectives extends CommonDirectives with BlogFormats with CommentFormats {
    def blogLinks = respondWithLinks(
        jsonLink("/blogs", "blogs", GET),
        mtLink("/blogs/{blogId}", "blog", `application/vnd.blog+json`, GET, PUT, DELETE))

    def commentLinks = respondWithLinks(
        jsonLink("/blogs/{blogId}/comments", "comments", GET),
        mtLink("/blogs/{blogId}/comments/{commentId}", "comment",
            `application/vnd.comment+json`, GET, PUT, DELETE))
}
// vim: set ts=4 sw=4 et:
