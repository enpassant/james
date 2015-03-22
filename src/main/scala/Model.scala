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
    val data = (for {
        x <- 1 to 100
    } yield Blog(UUID.randomUUID.toString, "jim", new DateTime().minusDays(x),
        s"Title ${x}", s"Description ${x}. Mode: ${config.mode}")).reverse

    def receive: Receive = process(data)

    def process(data: IndexedSeq[Blog]): Receive = {
        case GetEntity(uuid: String) =>
            sender ! data.find(_.id == uuid.toString)
        case ListWithOffset(Blog, params, offset, limit) =>
            sender ! EntityList(data.drop(offset).take(limit))
        case AddEntity(blog: Blog, _*) =>
            context.become(process(blog +: data))
            sender ! blog
        case DeleteEntity(id: String) =>
            val entity = data.find(_.id == id)
            context.become(process(data.filterNot(_.id == id)))
            sender ! entity
    }
}
// vim: set ts=4 sw=4 et:
