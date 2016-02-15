package core

import component._

import akka.actor.{ Actor, ActorRef, ActorSystem, Props }

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
            val superVisor = actorSystem.actorOf(Supervisor.props(config), Supervisor.name)

        case None =>
    }
}
// vim: set ts=4 sw=4 et:
