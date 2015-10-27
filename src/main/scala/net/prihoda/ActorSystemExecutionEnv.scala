package net.prihoda

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, Logging }
import akka.stream.{ ActorMaterializer, Materializer }

import scala.concurrent.ExecutionContext

trait ActorSystemExecutionEnv extends ExecutionEnv {
  protected implicit def system: ActorSystem

  override protected implicit lazy val executor: ExecutionContext = system.dispatcher
  override protected lazy val log: LoggingAdapter = Logging(system, getClass)
  override protected implicit lazy val materializer: Materializer = ActorMaterializer()
}
