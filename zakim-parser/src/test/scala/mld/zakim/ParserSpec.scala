//package mld.zakim
//
//import org.specs2.mutable.Specification
//
//class ParserSpec extends Specification {
//
//  // Credit to https://stackoverflow.com/a/12310078
//  def toBinaryString(b: Byte): String =
//    String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0')
//
//  "all colons" ! {
//    Parser.colonBitmap(Array.fill(10)(":".getBytes("UTF-8")).flatten)
//      .map(toBinaryString).mkString(",") ==== "11111111,00000011"
//  }
//
//  "not colons" ! {
//    Parser.colonBitmap(Array.fill(10)("$".getBytes("UTF-8")).flatten)
//      .map(toBinaryString).mkString(",") ==== "00000000,00000000"
//  }
//}
