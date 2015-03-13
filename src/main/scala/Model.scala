import java.util.UUID
import org.joda.time.DateTime
import akka.actor.Actor

case class ListWithOffset[T](t: AnyRef, offset: Int, limit: Int)
case class Blogs(slice: Iterable[Blog])
case class AddBlog(blog: Blog)

class Model extends Actor {
    // Dummy data for illustration purposes, in ascending order by date
    val data = (for {
        x <- 1 to 100
    } yield Blog(UUID.randomUUID.toString, "jim", new DateTime().minusDays(x),
        s"Title ${x}", s"Description ${x}")).reverse

    def receive: Receive = process(data)

    def process(data: IndexedSeq[Blog]): Receive = {
        case uuid: String =>
            sender ! data.find(_.id == uuid.toString)
        case ListWithOffset(Blog, offset, limit) =>
            sender ! Blogs(data.drop(offset).take(limit))
        case AddBlog(blog) =>
            context.become(process(blog +: data))
            sender ! blog
    }
}
// vim: set ts=4 sw=4 et:
