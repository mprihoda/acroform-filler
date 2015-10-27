package net.prihoda

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

object Main extends App with Config with Service with ActorSystemExecutionEnv {
  protected implicit lazy val system = ActorSystem()

  Http().bindAndHandle(routes, httpInterface, httpPort)
}
