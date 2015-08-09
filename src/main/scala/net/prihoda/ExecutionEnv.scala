package net.prihoda

import akka.event.LoggingAdapter
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

trait ExecutionEnv {
  protected implicit def executor: ExecutionContext
  protected implicit def materializer: Materializer
  protected def log: LoggingAdapter
}
