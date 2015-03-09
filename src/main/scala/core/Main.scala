package core

import akka.actor.{Actor, Props, ActorSystem}
import spray.routing.SimpleRoutingApp

object Main extends App with SimpleRoutingApp {
  implicit val actorSystem = ActorSystem()

  startServer(interface = "localhost", port = 8080) {
    get {
      path("hello") {
        complete {
          "Hello World!"
        }
      }
    }
  }
}
