package net.prihoda

import java.net.URLDecoder

import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Sink
import net.prihoda.pdf.{ SecurityConfig, FontConfig, AkkaHttpResolverComponent, AcroformFiller }
import spray.json.{ JsString, JsArray, JsObject }

trait Service extends BaseService with AcroformFiller with AkkaHttpResolverComponent with FontConfig with SecurityConfig {
  val routes = {
    path(Rest)(p => {
      val uri = URLDecoder.decode(p, "UTF-8")
      get {
        val result = for {
          source <- resolver.resolve(uri)
          fields <- source.via(pdfAcroFields).runWith(Sink.head)
        } yield JsObject(Map("fields" -> JsArray(fields.map(JsString.apply).toArray: _*)))
        complete(result)
      } ~ post {
        entity(as[String])(body => {
          import spray.json._
          val data = body.parseJson.asJsObject.fields.mapValues {
            case JsString(value) => value
            case _               => ""
          }
          val result = for {
            source <- resolver.resolve(uri)
            doc <- source.via(pdfFlattenedWith(data)).runWith(Sink.head)
          } yield doc
          complete(result)
        })
      }
    })
  }
}
