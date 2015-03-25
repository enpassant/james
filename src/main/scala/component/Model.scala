package component

import core._

import java.util.UUID
import org.joda.time.DateTime
import akka.actor.Actor

case class GetEntity[T](ids: String*)
case class ListWithOffset(t: Any, params: Seq[Any], offset: Int, limit: Int)
case class EntityList[T](slice: Iterable[T])
case class AddEntity[T](blog: T, ids: String*)
case class DeleteEntity(ids: String*)

class Model(val config: Config) extends Actor {
    // Dummy data for illustration purposes, in ascending order by date
    val tableBlog = (for {
        x <- 1 to 100
    } yield Blog(UUID.randomUUID.toString, "jim", new DateTime().minusDays(x),
        s"Title ${x}", s"Description ${x}. Mode: ${config.mode}")).reverse

    def receive: Receive = process(tableBlog, Map())

    def process(tableBlog: IndexedSeq[Blog], tableComment: Map[String, IndexedSeq[Comment]]): Receive = {
        case GetEntity(uuid) =>
            sender ! tableBlog.find(_.id == uuid.toString)
        case ListWithOffset(Blog, _, offset, limit) =>
            sender ! EntityList(tableBlog.drop(offset).take(limit))
        case AddEntity(blog: Blog, _*) =>
            context.become(process(blog +: tableBlog, tableComment))
            sender ! blog
        case DeleteEntity(id) =>
            val entity = tableBlog.find(_.id == id)
            context.become(process(tableBlog.filterNot(_.id == id), tableComment))
            sender ! entity

        case GetEntity(blogId, commentId) =>
            sender ! tableComment(blogId).find(_.id == commentId.toString)
        case ListWithOffset(Comment, Seq(blogId: String), offset, limit) =>
            if (tableComment contains blogId) {
                sender ! EntityList(tableComment(blogId).drop(offset).take(limit))
            } else {
                sender ! EntityList(IndexedSeq.empty[Comment])
            }
        case AddEntity(comment: Comment, ids @ _*) =>
            val blogId = ids.head
            if (tableComment contains blogId) {
                val comments = tableComment(blogId)
                context.become(process(tableBlog, tableComment + (blogId -> (comment +: comments))))
            } else {
                context.become(process(tableBlog, tableComment + (blogId -> IndexedSeq(comment))))
            }
            sender ! comment
        case DeleteEntity(blogId, commentId) =>
            val entity = tableComment(blogId).find(_.id == commentId)
            val comments = tableComment(blogId).filterNot(_.id == commentId)
            context.become(process(tableBlog, tableComment + (blogId -> comments)))
            sender ! entity
    }
}
// vim: set ts=4 sw=4 et:
