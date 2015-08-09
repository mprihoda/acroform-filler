package net.prihoda

import java.net.URLDecoder

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import net.prihoda.pdf.{ FontConfig, AkkaHttpResolverComponent, AcroformFiller }
import spray.json.{ JsString, JsArray, JsObject }

trait Service extends BaseService with AcroformFiller with AkkaHttpResolverComponent with FontConfig {
  val routes = {
    path(Rest)(p => {
      val uri = URLDecoder.decode(p, "UTF-8")
      get {
        val result = for {
          source <- resolver.resolve(uri)
          fields <- source.via(pdfAcroFields).runWith(Sink.head)
        } yield JsObject(Map("fields" -> JsArray(fields.map(JsString.apply).toArray: _*)))
        complete(result)
      }
    })
  }
}
