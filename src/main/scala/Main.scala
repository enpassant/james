import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import spray.can.Http
import akka.io.IO

object Main extends App {
    implicit val actorSystem = ActorSystem("blog")

    val myApp: ActorRef = actorSystem.actorOf(Props[BaseActor])
    myApp ! "start"

    val model = actorSystem.actorOf(Props[Model])
    class BaseActor extends Actor {
        def receive: Receive = {
            case "start" =>
                val service = actorSystem.actorOf(Props(new Service(model)))
                IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
        }
    }
}
// vim: set ts=4 sw=4 et:
