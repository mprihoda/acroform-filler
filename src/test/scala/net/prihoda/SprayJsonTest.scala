package net.prihoda

import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsString, JsonParser, ParserInput}

class SprayJsonTest extends WordSpec with Matchers {

  "Spray json support" should {
    "Parser an UTF-8 string" in pendingUntilFixed({
      val name = "Český Metrologický Institut"
      val jsObject = JsonParser(ParserInput(s"""{"name":"$name"}"""".getBytes("UTF-8"))).asJsObject
      jsObject.fields("name") match {
        case s: JsString => s.value should equal(name)
        case _ => fail("The value should be a string")
      }
    })
  }

}
