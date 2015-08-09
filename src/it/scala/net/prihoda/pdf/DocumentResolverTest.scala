package net.prihoda
package pdf

import java.security.MessageDigest

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
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
      val future = for {
        source <- resolver.resolve(locator)
        document <- source.runWith(Sink.head)
      } yield document

      val document = Await.result(future, 20.seconds)

      document should not be 'empty
      document.length should be(356820)
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }
}
