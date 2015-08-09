package net.prihoda
package pdf

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.Future

trait DocumentResolverComponent {

  type DocumentLocator = String
  type Document = Source[ByteString, _]

  def resolver: DocumentResolver

  trait DocumentResolver {
    def resolve(locator: DocumentLocator): Future[Document]
  }

}

trait AkkaHttpResolverComponent extends DocumentResolverComponent {
  self: ExecutionEnv =>

  protected implicit def system: ActorSystem

  override lazy val resolver = new AkkaHttpResolver

  class AkkaHttpResolver extends DocumentResolver {
    def resolve(locator: DocumentLocator): Future[Document] = {
      for (response <- Http().singleRequest(HttpRequest(HttpMethods.GET, locator))) yield {
        response.entity.dataBytes
      }
    }

  }

}
