package mld.zakim

import java.nio.ByteBuffer

import org.specs2.matcher.MatchResult
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

import io.Source

class ParserSpec extends Specification {
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

  private def assertPositions[T](
    resource: String,
    f: List[(JsonPath, Position)] => MatchResult[T]): MatchResult[T] =
    f(Parser.parse(
      loadJson(resource), List.empty[(JsonPath, Position)]) {
      case (path, p, pathToPosition) => (path -> p) :: pathToPosition
    }.reverse)

  "without whitespace" >> {
    def scenario(name: String): Fragment =
      name ! assertPositions(withoutWhitespace(s"$name.json"), _ ====
        loadResult(withoutWhitespace(s"$name.result")))
    scenario("empty")
    scenario("flat-01")
    scenario("flat-02")
    scenario("flat-03")
    scenario("nested-01")
    scenario("nested-02")
    scenario("nested-03")
    scenario("nested-04")
    scenario("nested-05")
  }

  "with whitespace" >> {
    def scenario(name: String): Fragment =
      name ! assertPositions(withWhitespace(s"$name.json"), _ ====
        loadResult(withWhitespace(s"$name.result")))
    scenario("flat-01")
    scenario("flat-02")
    scenario("flat-03")
    scenario("nested-01")
    scenario("nested-02")
  }
}
