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
  case object LookingForNextTerm extends State

  def parse[T](json: ByteBuffer, init: T)
    (f: (JsonPath, Position, T) => T): T = {
    def doParse(
      s: State,
      keys: List[String],
      t: T): T = {
      logger.debug(s"State = $s")
      if (json.limit() == json.position())
        t
      else {
        val z = json.get().toChar
        z match {
          case ' ' | '\t' | '\n' | '\r' =>
            logger.debug(s"Removing whitespace still in $s")
            doParse(s, keys, t)
          case _ =>
            logger.debug(s"Next up we have = $z")
            json.position(json.position() - 1)
            s match {
              case StartOfObject =>
                val s = json.get().toChar
                s match {
                  case '{' => doParse(StartOfKey, keys, t)
                  case x => sys.error(s"Unexpected state! $x")
                }

              case StartOfKey =>
                val z = json.get().toChar
                z match {
                  case '"' =>
                    var k = json.get()
                    val sb = new StringBuilder(k.toChar)
                    var reverseKey: List[Byte] = Nil
                    while (k != '"') {
                      reverseKey = k :: reverseKey
                      k = json.get()
                      sb.append(k.toChar)
                    }
                    logger.debug(s"Found key = $sb")
                    doParse(EndOfKey,
                      new String(reverseKey.reverse.toArray, "UTF-8") :: keys,
                      t)
                  case '}' => doParse(EndOfKey, keys, t) // Empty object
                  case _ => sys.error(s"Unsupported = $z " +
                    s"at position = ${json.position() - 1}")
                }

              case EndOfKey =>
                val z = json.get().toChar
                z match {
                  case ':' => doParse(LookingForNextTerm, keys, t)
                  case _ => sys.error(
                    s"Unexpected state! = [${z.toChar}]  " +
                      s"at position = ${json.position() - 1}")
                }
              case LookingForNextTerm =>
                val s = json.get().toChar
                if (s == '{') {
                  json.position(json.position() - 1)
                  doParse(StartOfObject, keys, t)
                } // found object
                else if (s == '[') doParse(StartOfArray, keys, t)
                // Making assumption it's a null, boolean, or number
                // (not whitespace)
                else {
                  json.position(json.position() - 1)
                  doParse(StartOfValue, keys, t)
                }
              case StartOfString =>
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
                  Position(startIndex, endIndex), t))
              case EndOfString =>
                val z = json.get()
                z match {
                  case ',' => doParse(StartOfKey, keys.tail, t)
                  case '}' => doParse(EndOfObject, keys.tail, t)
                  case x => sys.error(s"Unsupported state = $x " +
                    s"at position = ${json.position() - 1}")
                }
              case StartOfValue =>
                val first = json.get(json.position())
                val startIndex =
                  if (first == '"') StartIndex(json.position())
                  else StartIndex(json.position())
                var kPrev = json.get()
                var k = kPrev
                logger.debug(s"first = ${first.toChar} and k = ${k.toChar}")

                var reversedValue = List.empty[Byte]
                val sb = new StringBuilder(k.toChar.toString)
                if (first == '"') {
                  // String
                  k = json.get() // Perform read to get next byte after opening
                  // quote (")
                  while (k != '"' && k != '}' && kPrev != '\\') {
                    reversedValue = k :: reversedValue
                    kPrev = k
                    sb.append(k.toChar)
                    k = json.get()
                  }
                } else {
                  while (k != ',' && k != '}' && k.toChar != '\n') {
                    reversedValue = k :: reversedValue
                    kPrev = k
                    sb.append(k.toChar)
                    k = json.get()
                  }
                }

                logger.debug(s"value read = $sb ... k = ${k.toChar} at position = ${json.position()}")
                logger.debug(s"we got at 8 = ${json.get(8).toChar} and at 10 = ${json.get(10).toChar}")

                val endIndex = if (k == '}' || k == ',') {
                  // At this point, we are two chars ahead of the last char
                  // that we want to track.
                  // Minus 1 to reset cursor to the next thing to look for and
                  // minus 2 to grab correct last index
                  json.position(json.position() - 1) // Side effect
                  EndIndex(json.position() - 1)
                } else if (k == '\n') {
                  EndIndex(json.position() - 2) // Do not count newline
                }
                else
                  EndIndex(json.position() - 1)

                doParse(EndOfValue, keys, f(
                  JsonPath(keys.reverse.mkString(JsonPath.Separator)),
                  Position(startIndex, endIndex), t))

              case EndOfValue =>
                val z = json.get().toChar
                z match {
                  case ',' => doParse(StartOfKey, keys.tail, t)
                  case '}' => doParse(EndOfObject, keys.tail, t)
                  case ']' =>
                    // Given "site":{"cat":["IAB1"],"page": "b"
                    // Then the "site" key should not be deleted when moving
                    // from IAB1 to page
                    doParse(EndOfArray, keys, t)
                  case x => sys.error(s"Unsupported state = $x " +
                    s"at position = ${json.position() - 1}")
                }

              case EndOfObject =>
                val z = json.get().toChar
                z match {
                  case ',' =>
                    // Can either be an object or a key
                    // Unclear if it can be an array
                    // Need to peak ahead in order to get enough context
                    val curPosition = json.position()
                    logger.debug(s"our position is = $curPosition")
                    val zz = json.get().toChar
                    zz match {
                      case '"' =>
                        json.position(curPosition)
                        doParse(StartOfKey, keys.tail, t)
                      case '{' =>
                        // Don't strip head key in this case
                        json.position(curPosition)
                        doParse(StartOfObject, keys, t)
                    }
                  case '}' =>
                    // Nested end of objects could happen inside an array. e.g.:
                    // {{"banner":{"w":740,"h":30}} , {"banner":{"w":320,"h":240}}]
                    doParse(EndOfObject, keys.tail, t)
                  case ']' =>
                    doParse(EndOfArray, keys, t)
                  case x => sys.error(s"Unsupported state = $x " +
                    s"at position = ${json.position() - 1}")
                }
              case StartOfArray =>
                // Peek ahead to figure out what's next
                val z = json.get().toChar
                json.position(json.position() - 1)
                z match {
                  case '{' => doParse(StartOfObject, keys, t)
                  // Assume it's an array of values
                  case _ => doParse(StartOfValue, keys, t)
                }
              case EndOfArray =>
                logger.debug(s"Position = ${json.position()}")
                json.get().toChar match {
                  case ',' =>
                    // Given: {"imp":[{"id":"1"}],"page":"http://google.com"}
                    // Want to remove 'imp' as key when reaching "}]"
                    doParse(StartOfKey, keys.tail, t)
                  case '}' => doParse(EndOfObject, keys.tail, t)
                  case x => sys.error(s"Unsupported state = $x " +
                    s"at position = ${json.position() - 1}")
                }
            }
        }
      }

    }
    doParse(StartOfObject, Nil, init)
  }
}
