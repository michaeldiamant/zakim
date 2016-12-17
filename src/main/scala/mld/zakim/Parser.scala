package mld.zakim

import java.nio.ByteBuffer

import com.typesafe.scalalogging.StrictLogging

object Parser extends StrictLogging {
  sealed trait State
  case object StartOfObject extends State
  case object StartOfKey extends State
  case object EndOfKey extends State
  case object StartOfString extends State
  case object EndOfString extends State
  case object StartOfValue extends State
  case object EndOfValue extends State
  case object EndOfObject extends State
  case object StartOfArray extends State
  case object EndOfArray extends State

  def parse[T](json: ByteBuffer, init: T)
    (f: (JsonPath, Position, T) => T): T = {
    def doParse(
      s: State,
      keys: List[String],
      t: T,
      currentKey: List[Byte]): T = s match {
      case StartOfObject =>
        logger.debug("==> StartOfObject")
        val s = json.get().toChar
        s match {
          case ' ' | '\t' | '\n' | '\r' => doParse(StartOfObject, keys, t, Nil)
          case '{' => doParse(StartOfKey, keys, t, Nil)
          case x => sys.error(s"Unexpected state! $x")
        }

      case StartOfKey =>
        logger.debug("==> StartOfKey")
        val z = json.get().toChar
        z match {
          case ' ' | '\t' | '\n' | '\r' => doParse(StartOfKey, keys, t, currentKey)
          case '"' =>
            var k = json.get()
            logger.debug(s">> Found ${k.toChar}")
            var reverseKey: List[Byte] = Nil
            while (k != '"') {
              reverseKey = k :: reverseKey
              k = json.get()
            }
            doParse(EndOfKey,
              new String(reverseKey.reverse.toArray, "UTF-8") :: keys, t,
              reverseKey)
          case _ => sys.error(s"Unsupported = $z")
        }

      case EndOfKey =>
        logger.debug("==> EndOfKey")
        val z = json.get().toChar
        z match {
          case ' ' | '\t' | '\n' | '\r' => doParse(EndOfKey, keys, t, currentKey)
          case ':' =>
            val s = json.get()
            //            if (s == '"'.toByte) doParse(StartOfString, keys, t, currentKey, m, keyToPosition)
            if (s == '{') {
              json.position(json.position() - 1)
              doParse(StartOfObject, keys, t, currentKey)
            } // found object
            else if (s == '[') doParse(StartOfArray, keys, t, Nil)
            // Making assumption it's a null, boolean, or number (not whitespace)
            else doParse(StartOfValue, keys, t, currentKey)
          case _ => sys.error(s"Unexpected state! = [${z.toChar}] at ${json.position()}")
        }
      case StartOfString =>
        logger.debug("==> StartOfString")
        val startIndex = StartIndex(json.position())
        var k = json.get()

        var reversedValue = List.empty[Byte]
        while (k != '"') {
          reversedValue = k :: reversedValue
          k = json.get()
        }
        val endIndex = EndIndex(json.position() - 2)
        doParse(EndOfString, keys, f(
          JsonPath(keys.reverse.mkString(JsonPath.Separator)),
          Position(startIndex, endIndex), t),
          Nil)
      case EndOfString =>
        logger.debug("==> EndOfString")
        if (json.limit() == json.position())
          t
        else {
          val z = json.get()
          z match {
            case ',' => doParse(StartOfKey, keys.tail, t, Nil)
            case '}' => doParse(EndOfObject, keys.tail, t, Nil)
            case x => sys.error(s"Unsupported state = $x")
          }
        }
      case StartOfValue =>
        logger.debug("==> StartOfValue")
        val startIndex = StartIndex(json.position() - 1)
        var kPrev = json.get()
        var k = kPrev

        var reversedValue = List.empty[Byte]
        while ((k != ',' && k != '}') && kPrev != '"') {
          reversedValue = k :: reversedValue
          kPrev = k
          k = json.get()
        }
        val endIndex = EndIndex(json.position() - 1)
        json.position(json.position() - 1) // Side effect
        doParse(EndOfValue, keys, f(
          JsonPath(keys.reverse.mkString(JsonPath.Separator)),
          Position(startIndex, endIndex), t),
          Nil)

      case EndOfValue =>
        logger.debug("==> EndOfValue")
        if (json.limit() == json.position())
          t
        else {
          val z = json.get().toChar
          z match {
            case ',' => doParse(StartOfKey, keys.tail, t, Nil)
            case '}' =>
              doParse(EndOfObject, keys.tail, t, Nil)
            case ']' => doParse(EndOfArray, keys.tail, t, Nil)
            case x => sys.error(s"Unsupported state = $x ... ${new String(new Array[Byte](x), "UTF-8")}")
          }
        }

      case EndOfObject =>
        logger.debug("==> EndOfObject")
        if (json.limit() == json.position())
          t
        else {
          val z = json.get().toChar
          z match {
            case ' ' | '\t' | '\n' | '\r' => doParse(EndOfObject, keys, t, currentKey)
            case ',' =>
              // Can either be an object or a key
              // Unclear if it can be an array
              // Need to peak ahead in order to get enough context
              val curPosition = json.position()
              val zz = json.get().toChar
              zz match {
                case '"' =>
                  json.position(curPosition)
                  doParse(StartOfKey, keys.tail, t, Nil)
                case '{' =>
                  // Don't strip head key in this case
                  json.position(curPosition)
                  doParse(StartOfObject, keys, t, Nil)
              }
            case '}' =>
              // Nested end of objects could happen inside an array. e.g.:
              // {{"banner":{"w":740,"h":30}} , {"banner":{"w":320,"h":240}}]
              doParse(EndOfObject, keys, t, Nil)
            case ']' =>
              logger.debug("Dropping " + keys.headOption + " ... leaving " + keys.tail)
              doParse(EndOfArray, keys.tail, t, Nil)
            case x => sys.error(s"Unsupported state = ${x.toChar} at position = ${json.position()}")
          }
        }
      case StartOfArray =>
        logger.debug("==> StartOfArray")
        val z = json.get().toChar
        z match {
          case ' ' | '\t' | '\n' | '\r' => doParse(StartOfArray, keys, t, currentKey)
          case '{' =>
            json.position(json.position() - 1)
            doParse(StartOfObject, keys, t, Nil)
          // Assume it's an array of values
          case _ => doParse(StartOfValue, keys, t, Nil)
        }
      case EndOfArray =>
        logger.debug("==> EndOfArray")
        val z = json.get().toChar
        z match {
          case ',' => doParse(StartOfKey, keys.tail, t, Nil)
          case '}' =>
            doParse(EndOfObject, keys.tail, t, Nil)
          case x => sys.error(s"Unsupported state = $x ... ${new String(new Array[Byte](x), "UTF-8")}")
        }
    }
    doParse(StartOfObject, Nil, init, Nil)
  }
}
