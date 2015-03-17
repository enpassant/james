import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import spray.can.Http
import akka.io.IO

object Main extends App {
    implicit val actorSystem = ActorSystem("james")

    case class Config(host: String = "localhost", port: Int = 8080, mode: String = "dev")

    val parser = new scopt.OptionParser[Config]("james") {
        head("james", "1.0")
        opt[String]('h', "host") action { (x, c) =>
            c.copy(host = x) } text("host name or address. Default: localhost")
        opt[Int]('p', "port") action { (x, c) =>
            c.copy(port = x) } text("port number. Default: 8080")
        opt[String]('m', "mode") action { (x, c) =>
            c.copy(mode = x) } text("running mode. e.g.: dev, test, prod. Default: dev")
    }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
        case Some(config) =>
            val model = actorSystem.actorOf(Props(new Model(config.mode)))
            class BaseActor extends Actor {
                def receive: Receive = {
                    case "start" =>
                        println(config)
                        val service = actorSystem.actorOf(Props(new Service(config.mode, model)))
                        IO(Http) ! Http.Bind(service, interface = config.host, port = config.port)
                }
            }

            val myApp: ActorRef = actorSystem.actorOf(Props(new BaseActor))
            myApp ! "start"

        case None =>
    }
}
// vim: set ts=4 sw=4 et:
