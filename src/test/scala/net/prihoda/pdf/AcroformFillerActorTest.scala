package net.prihoda.pdf

import java.io.FileOutputStream

import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestActorRef, TestKitBase}
import akka.util.{Timeout, ByteString}
import org.apache.commons.io.IOUtils
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers, WordSpec}

import scala.util.Success

class AcroformFillerActorTest
  extends TestKit(ActorSystem("testsystem")) with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  import AcroformFiller._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  //implicit val timeout = Timeout(20.seconds)

  val filler = system.actorOf(Props(classOf[AcroformFillerActor], ConfigFactory.load()))

  val document: Document = ByteString(IOUtils.toByteArray(getClass.getResourceAsStream("/nyan_form.pdf")))

  "AcroformFiller actor" should {

    "return a document handle for a PDF" in {
      filler ! HandleDocument(document)
      expectMsgPF() {
        case h:DocumentHandle => h should equal(DocumentHandle(document))
      }
    }

    "return an error when asked to handle invalid file" is pending

    "return a prepared document for correct handle" in {
      filler ! HandleDocument(document)
      val handle = expectMsgClass(classOf[DocumentHandle])

      filler ! PrepareDocument(handle)
      expectMsgPF() {
        case Some(PreparedDocument(fields)) =>
          fields should have size 2
          fields should contain("HELLO")
          fields should contain("WORLD")
      }
    }

    "not return a prepared document for unknown handle" is pending

    "render a PDF from prepared document and data" in {
       filler ! HandleDocument(document)
      val handle = expectMsgClass(classOf[DocumentHandle])

      filler ! RenderDocument(handle, Map("HELLO" -> "Ahoj", "WORLD" -> "Světe"))
      expectMsgPF() {
        case Some(RenderedDocument(pdf)) =>
          pdf.length should be > 0
          IOUtils.write(pdf.toArray, new FileOutputStream("./target/rendered-output.pdf"))
      }
    }

  }

}
