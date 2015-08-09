package net.prihoda
package pdf

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import org.scalatest.{BeforeAndAfterAll, WordSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestKitBase}

class DocumentResolverTest extends WordSpec with TestKitBase with Matchers with AkkaHttpResolverComponent with ActorSystemExecutionEnv with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem("testsystem")

  "Document resolver" should {
    "resolve a document by URL" in {
      val locator = "http://www.acaeid.cz/Files/D35-Zprava_pro_uzivatele-1.2.pdf"
      val document = for {
        source <- resolver.resolve(locator)
        document <- source.runWith(Sink.head)
      } yield document

      Await.result(document, 20.seconds) should not be 'empty
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
