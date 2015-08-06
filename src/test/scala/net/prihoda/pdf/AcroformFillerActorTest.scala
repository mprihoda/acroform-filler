package net.prihoda.pdf

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKitBase}
import akka.util.{Timeout, ByteString}
import org.apache.commons.io.IOUtils
import org.scalatest.{Matchers, WordSpec}

import scala.util.Success

class AcroformFillerActorTest extends WordSpec with Matchers with TestKitBase {

  import AcroformFiller._

  implicit lazy val system = ActorSystem("testsystem")
  implicit val timeout = Timeout(20.seconds)

  val filler = TestActorRef[ActorformFillerActor]

  "AcroformFiller actor" should {

    "return a document handle for a PDF" in {
      val document: Document = ByteString(IOUtils.toByteArray(getClass.getResourceAsStream("/nyan_form.pdf")))
      val future = filler ? HandleDocument(document)
      val Success(handle: DocumentHandle) = future.value.get

      handle should equal(DocumentHandle(document))
    }

    "return an error when asked to handle invalid file" is pending
    "return a prepared document for correct handle" is pending
    "not return a prepared document for unknown handle" is pending
    "render a PDF from prepared document and data" is pending

  }

}
