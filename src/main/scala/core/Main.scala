package core

import component._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import spray.can.Http
import akka.io.IO

case class Config(host: String = "localhost", port: Int = 9000,
    router: Option[String] = None, mode: Option[String] = None)

object Main extends App {
    implicit val actorSystem = ActorSystem("james")

    val parser = new scopt.OptionParser[Config]("james") {
        head("james", "1.0")
        opt[String]('h', "host") action { (x, c) =>
            c.copy(host = x) } text("host. Default: localhost")
        opt[Int]('p', "port") action { (x, c) =>
            c.copy(port = x) } text("port number. Default: 9000")
        opt[String]('r', "router") action { (x, c) =>
            c.copy(router = Some(x)) } text("router's host and port. e.g.: localhost:9101")
        opt[String]('m', "mode") action { (x, c) =>
            c.copy(mode = Some(x)) } text("running mode. e.g.: dev, test")
     }

    // parser.parse returns Option[C]
    parser.parse(args, Config()) match {
        case Some(config) =>
            val tickActor = config.router map {
                _ => actorSystem.actorOf(Props(new TickActor(config)))
            }
            tickActor.map(_ ! Tick)

            val model = actorSystem.actorOf(Props(new Model(config)))
            val service = actorSystem.actorOf(Props(new Service(config, model, tickActor)))

        case None =>
    }
}
// vim: set ts=4 sw=4 et:
