package mld.zakim

import java.io.{InputStream, PrintWriter}

import scala.io.Source
import scala.util.Try

object Result {
  private val Sep = '\t'

  def toTsv(t: (JsonPath, Position)): String = {
    val (path, position) = t
    List(path.value, position.start.value, position.end.value)
      .mkString(Sep.toString)
  }

  def fromTsv(l: String): Option[(JsonPath, Position)] =
    l.split(Sep).toList match {
      case path :: start :: end :: Nil =>
        for {
          s <- Try(start.toInt).toOption.map(StartIndex.apply)
          e <- Try(end.toInt).toOption.map(EndIndex.apply)
        } yield (JsonPath(path), Position(s, e))
      case _ => None
    }

  def write(filename: String, xs: List[(JsonPath, Position)]): Unit = {
    val w = new PrintWriter(filename)
    try xs.foreach(x => w.write(s"${toTsv(x)}\n")) finally w.close()
  }

  def read(is: InputStream): List[(JsonPath, Position)] = {
    val s = Source.fromInputStream(is)
    try s.getLines().toList.flatMap(fromTsv) finally s.close()
  }
}
