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
  val font = fontPath.map(path => BaseFont.createFont(path, BaseFont.CP1250, true))
}

trait SecurityConfig {
  self: net.prihoda.Config =>

  val documentPassword = Try(config.getString("acroform.password")).toOption
}

trait AcroformFiller {
  self: FontConfig with SecurityConfig =>

  private def textFields(reader: PdfReader) =
    reader.getAcroFields.getFields.keySet().asScala.toSet.filter(!_.endsWith(":signature"))

  val pdfAcroFields: Flow[ByteString, Set[String], Unit] =
    Flow[ByteString]
      .map(document => {
        val reader = new PdfReader(document.toArray)
        textFields(reader)
      })

  def pdfFlattenedWith(data: Map[String, String]): Flow[ByteString, ByteString, Unit] =
    Flow[ByteString]
      .map(document => {
        val out = new ByteArrayOutputStream
        val reader = documentPassword match {
          case Some(pwd) => new PdfReader(document.toArray, pwd.getBytes("UTF-8"))
          case None => new PdfReader(document.toArray)
        }
        val stamper = new PdfStamper(reader, out)
        val fields = stamper.getAcroFields
        font.foreach(fields.addSubstitutionFont)
        for ((key, value) <- data) fields.setField(key, value)
        textFields(reader).foreach(stamper.partialFormFlattening)
        stamper.setFormFlattening(true)
        stamper.close()
        ByteString(out.toByteArray)
      })
}

