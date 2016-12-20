package mld.zakim

import java.nio.ByteBuffer

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import scala.io.Source

class ParserExtractorSpec extends Specification {
  private def withoutWhitespace(filename: String): String =
    s"example-json/without-whitespace/$filename"
  private def withWhitespace(filename: String): String =
    s"example-json/with-whitespace/$filename"

  private def loadJson(resource: String): ByteBuffer = {
    val s = Source.fromInputStream(
      getClass.getClassLoader.getResourceAsStream(resource))
    try ByteBuffer.wrap(
      s.getLines().toList.mkString("\n").getBytes("UTF-8"))
    finally s.close()
  }

  private def loadResult(resource: String): List[(JsonPath, Position)] =
    Result.read(getClass.getClassLoader.getResourceAsStream(resource))

  "flat-01" ! {
    val j = loadJson(withoutWhitespace("flat-01.json"))
    val m = loadResult(withoutWhitespace("flat-01.result")).toMap

    Extractors.stringUnsafely(j, m(JsonPath("regs"))) ==== "0" and
      Extractors.stringUnsafely(j, m(JsonPath("w"))) ==== "728" and
      Extractors.stringUnsafely(j, m(JsonPath("h"))) ==== "90"
  }

  "flat-03" ! {
    val j = loadJson(withoutWhitespace("flat-03.json"))
    val m = loadResult(withoutWhitespace("flat-03.result")).toMap

    Extractors.intUnsafely(j, m(JsonPath("w"))) ==== 728 and
      Extractors.intUnsafely(j, m(JsonPath("h"))) ==== 90
  }
}
