package net.prihoda.pdf

import java.io.ByteArrayOutputStream

import akka.stream.scaladsl.Flow
import akka.util.ByteString
import com.itextpdf.text.pdf.{ BaseFont, PdfReader, PdfStamper }

import scala.collection.JavaConverters._
import scala.collection.immutable.Set
import scala.util.Try

trait FontConfig {
  self: net.prihoda.Config =>

  val fontPath = Try(config.getString("acroform.substitutionFont")).toOption
  val font = fontPath.map(path => BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED))
}

trait AcroformFiller {
  self: FontConfig =>

  val pdfAcroFields: Flow[ByteString, Set[String], Unit] =
    Flow[ByteString]
      .map(document => {
        val reader = new PdfReader(document.toArray)
        reader.getAcroFields.getFields.keySet().asScala.toSet
      })

  def pdfFlattenedWith(data: Map[String, String]): Flow[ByteString, ByteString, Unit] =
    Flow[ByteString]
      .map(document => {
        val out = new ByteArrayOutputStream
        val reader = new PdfReader(document.toArray)
        val stamper = new PdfStamper(reader, out)
        val fields = stamper.getAcroFields
        font.foreach { f =>
          for (entry <- fields.getFields.entrySet().asScala) {
            fields.setFieldProperty(entry.getKey, "textfont", f, null)
          }
        }
        for ((key, value) <- data) fields.setField(key, value)
        stamper.setFormFlattening(true)
        stamper.close()
        ByteString(out.toByteArray)
      })
}

