package net.prihoda.pdf

import java.security.MessageDigest

import akka.actor.{ActorLogging, Actor}
import akka.actor.Actor.Receive
import akka.actor.Status.Failure
import akka.util.ByteString

object AcroformFiller {

  type Document = ByteString
  type DocumentDigest = ByteString

  sealed trait Request

  case class HandleDocument(document: Document) extends Request

  sealed trait Response

  case class DocumentHandle(size: Long, digest: DocumentDigest) extends Response

  object DocumentHandle {
    def apply(document: Document): DocumentHandle = DocumentHandle(document.length, {
      val md = MessageDigest.getInstance("SHA-256")
      ByteString(md.digest(document.toArray))
    })
  }

}

class ActorformFillerActor extends Actor with ActorLogging {

  import AcroformFiller._

  override def receive: Receive = {
    case HandleDocument(doc) => sender ! DocumentHandle(doc)
  }
}
