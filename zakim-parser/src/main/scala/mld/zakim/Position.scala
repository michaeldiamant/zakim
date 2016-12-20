package mld.zakim

case class Position(start: StartIndex, end: EndIndex) {
  def length: Int = end.value - start.value + 1
}
