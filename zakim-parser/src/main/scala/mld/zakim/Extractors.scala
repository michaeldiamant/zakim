package mld.zakim

import java.nio.ByteBuffer
import java.lang.{Double => JDouble}

object Extractors {

  // TODO Introduce String extractor that allows re-use of Array[Byte]
  def stringUnsafely(b: ByteBuffer, p: Position): String = {
    // TODO Who has responsibility for resetting position?
    val mark = b.position()
    val arr = new Array[Byte](p.length - 2)
    b.position(p.start.value + 1)
    b.get(arr)
    b.position(mark)
    new String(arr, "UTF-8")
  }

  // TODO Re-write without use of String
  def intUnsafely(b: ByteBuffer, p: Position): Int = {
    val mark = b.position()
    val arr = new Array[Byte](p.length)
    b.position(p.start.value)
    b.get(arr)
    b.position(mark)
    Integer.parseInt(new String(arr, "UTF-8"))
  }

  def doubleUnsafely(b: ByteBuffer, p: Position): Double = {
    val mark = b.position()
    val arr = new Array[Byte](p.length)
    b.position(p.start.value)
    b.get(arr)
    b.position(mark)
    JDouble.parseDouble(new String(arr, "UTF-8"))
  }
}
