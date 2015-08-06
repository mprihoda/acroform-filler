package net.prihoda

import akka.http.scaladsl.server.Directives._

trait Service extends BaseService {
  val routes = get {
    log.info("/ executed")
    complete("OK")
  }
}
