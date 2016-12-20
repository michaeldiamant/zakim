package mld.zakim

case class JsonPath(value: String) extends AnyVal
object JsonPath {
  val Separator: String = "."
}
