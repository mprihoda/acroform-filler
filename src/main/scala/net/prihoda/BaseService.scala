package net.prihoda

import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.Materializer
import scala.concurrent.ExecutionContext

trait BaseService extends Protocol with SprayJsonSupport with Config {
  protected implicit def executor: ExecutionContext
  protected implicit def materializer: Materializer
  protected def log: LoggingAdapter
}
