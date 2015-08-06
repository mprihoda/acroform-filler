package net.prihoda.pdf

import java.security.MessageDigest

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.util.{Timeout, ByteString}
import com.itextpdf.text.pdf.PdfReader

import collection.immutable.Set

import akka.pattern.{ask, pipe}

import scala.concurrent.Future
import scala.concurrent.duration._

import scala.collection.JavaConverters._

object AcroformFiller {

  type Document = ByteString
  type DocumentDigest = ByteString
  type FieldData = Map[String, String]

  sealed trait Request

  case class HandleDocument(document: Document) extends Request

  abstract class DocumentRequest(val handle: DocumentHandle) extends Request

  case class PrepareDocument(_handle: DocumentHandle) extends DocumentRequest(_handle)

  case class RenderDocument(_handle: DocumentHandle, data: FieldData) extends DocumentRequest(_handle)

  sealed trait Response

  case class DocumentHandle(size: Long, digest: DocumentDigest) extends Response

  object DocumentHandle {
    def apply(document: Document): DocumentHandle = DocumentHandle(document.length, {
      val md = MessageDigest.getInstance("SHA-256")
      ByteString(md.digest(document.toArray))
    })
  }

  case class PreparedDocument(fields: Set[String]) extends Response

  case class RenderedDocument(document: Document) extends Response

}

class AcroformDocumentActor(document: AcroformFiller.Document) extends Actor with ActorLogging {

  import AcroformFiller._

  override def receive: Receive = {
    case PrepareDocument(_) =>
      val reader = new PdfReader(document.toArray)
      sender() ! PreparedDocument(reader.getAcroFields.getFields.keySet().asScala.toSet)
  }
}

class AcroformFillerActor extends Actor with ActorLogging {

  import AcroformFiller._

  import context.dispatcher

  implicit val timeout = Timeout(60.seconds)

  override def receive: Receive = route(Map.empty)

  // TODO: clean up unused the document actors

  private def route(documentActors: Map[DocumentHandle, ActorRef]): Receive = {
    case HandleDocument(doc) =>
      val handle = DocumentHandle(doc)
      sender() ! handle
      context.become(route(documentActors + (handle -> context.actorOf(Props(classOf[AcroformDocumentActor], doc)))))
    case request: DocumentRequest => process(request, documentActors.get)
  }

  private def process(request: DocumentRequest, actorFactory: (DocumentHandle) => Option[ActorRef]) =
    actorFactory(request.handle) match {
      case Some(actorRef) => (actorRef ? request).map(Option(_)) pipeTo sender()
      case None => sender() ! None
    }
}
