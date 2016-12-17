package mld.zakim

import java.nio.ByteBuffer

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification

import io.Source

class ParserSpec extends Specification {
  private def withoutWhitespace(filename: String): String =
    s"example-json/without-whitespace/$filename"
  private def position(start: Int, end: Int): Position =
    Position(StartIndex(start), EndIndex(end))

  private def loadJson(resource: String): ByteBuffer = {
    val s = Source.fromInputStream(
      getClass.getClassLoader.getResourceAsStream(resource))
    try ByteBuffer.wrap(
      s.getLines().toList.mkString("\n").getBytes("UTF-8"))
    finally s.close()
  }

  private def assertPositions[T](
    resource: String,
    f: List[(JsonPath, Position)] => MatchResult[T]): MatchResult[T] =
    f(Parser.parse(
      loadJson(resource), List.empty[(JsonPath, Position)]) {
      case (path, p, pathToPosition) => (path -> p) :: pathToPosition
    }.reverse)

  "without whitespace flat #1" ! assertPositions(
    withoutWhitespace("flat-01.json"),
    _ ==== List(
      (JsonPath("regs"), position(8, 11)),
      (JsonPath("w"), position(16, 21)),
      (JsonPath("h"), position(26, 30))))

  "without whitespace nested object #1" ! assertPositions(
    withoutWhitespace("nested-object-01.json"),
    _ ==== List(
      (JsonPath("regs"), position(8, 11)),
      (JsonPath("adSize.w"), position(26, 31)),
      (JsonPath("adSize.h"), position(36, 40))))
}
