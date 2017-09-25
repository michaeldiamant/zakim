//package mld.zakim
//
//import com.typesafe.scalalogging.StrictLogging
//
//import scala.annotation.{switch, tailrec}
//import scala.util.control.Breaks._
//
//object Parser extends StrictLogging {
//  sealed trait State {
//    val label: Int
//    def doit[T](s: MutableState[T]): Unit
//  }
//  case object StartOfObject extends State {
//    val label: Int = 0
//    def doit[T](s: MutableState[T]): Unit =
//      doStartOfObject(s)
//  }
//  val A = StartOfObject.label
//  case object StartOfKey extends State {
//    val label: Int = 1
//    def doit[T](s: MutableState[T]): Unit =
//      doStartOfKey(s)
//  }
//  val B = StartOfKey.label
//  case object EndOfKey extends State {
//    val label: Int = 2
//    def doit[T](s: MutableState[T]): Unit =
//      doEndOfKey(s)
//  }
//  val C = EndOfKey.label
//  //  case object StartOfString extends State
//  //  case object EndOfString extends State
//  case object StartOfValue extends State {
//    val label: Int = 3
//    def doit[T](s: MutableState[T]): Unit =
//      doStartOfValue(s)
//  }
//  val D = StartOfValue.label
//  case object EndOfValue extends State {
//    val label: Int = 4
//    def doit[T](s: MutableState[T]): Unit =
//      doEndOfValue(s)
//  }
//  val E = EndOfValue.label
//  case object EndOfObject extends State {
//    val label: Int = 5
//    def doit[T](s: MutableState[T]): Unit =
//      doEndOfObject(s)
//  }
//  val F = EndOfObject.label
//  case object StartOfArray extends State {
//    val label: Int = 6
//    def doit[T](s: MutableState[T]): Unit =
//      doStartOfArray(s)
//  }
//  val G = StartOfArray.label
//  case object EndOfArray extends State {
//    val label: Int = 7
//    def doit[T](s: MutableState[T]): Unit =
//      doEndOfArray(s)
//  }
//  val H = EndOfArray.label
//  case object LookingForNextTerm extends State {
//    val label: Int = 8
//    def doit[T](s: MutableState[T]): Unit =
//      doLookingForNextTerm(s)
//  }
//  val I = LookingForNextTerm.label
//  case object LookingForNextFieldAfterEndOfObject extends State {
//    val label: Int = 9
//    def doit[T](s: MutableState[T]): Unit =
//      doLookingForNextFieldAfterEndOfObject(s)
//  }
//  val J = LookingForNextFieldAfterEndOfObject.label
//  case object StartOfArrayValue extends State {
//    val label: Int = 10
//    def doit[T](s: MutableState[T]): Unit =
//      doStartOfArrayValue(s)
//  }
//  val L = StartOfArrayValue.label
//  case object EndOfArrayValue extends State {
//    val label: Int = 11
//    def doit[T](s: MutableState[T]): Unit =
//      doEndOfArrayValue(s)
//  }
//  val K = EndOfArrayValue.label
//
//
//  private val EmptyPath: JsonPath = JsonPath("")
//
//  private class MutableState[T](
//    var s: State,
//    var cc: Byte,
//    var json: Array[Byte],
//    var pos: Int,
//    var keys: List[String],
//    var t: T)
//
//  def parse[T](b: Array[Byte], init: T)
//    (f: (Array[Byte], JsonPath, StartIndex, EndIndex, T) => T): T = {
//    doParse(new MutableState(StartOfObject, -1, b, 0, Nil, init))
//  }
//
//  private final def parseStartOfKey(json: Array[Byte], pos: Int): Int = {
//    //                    var reverseKey: List[Byte] = Nil
//    var localPosition = pos + 1
//    var k = json(localPosition)
//    while (k != '"') {
//      //                      reverseKey = k :: reverseKey
//      localPosition = localPosition + 1
//      k = json(localPosition)
//    }
//    localPosition
//  }
//
//  //  @inline
//  def findNextChar(k: Int, json: Array[Byte], prev: Byte): Boolean = {
//    val zz = nextChar(k)
//    if (zz == 1) true
//    else if (zz == 0) false
//    else {
//      if (prev == '\\') false else true
//    }
//
//    //    if (k == '"') -1
//    //    else if (k == '}') 0
//    //    else 1
//
//    //            lol(k)
//    //    if (!zz && prev == '\\') false else true
//
//    //    (nextChar(k): @switch) match {
//    //      case -1 => if (json(position - 1) == '\\') false else true
//    //      case 0 => false
//    //      case 1 => true
//    //    }
//  }
//
//
//  private val lol = {
//    val z = Array.fill[Boolean](256)(true)
//    z.update('"'.toInt, false)
//    z.update('}'.toInt, false)
//    z
//  }
//
//  //  @inline
//  private final def nextChar(k: Int): Int =
//  //    (k: @switch) match {
//  //      case '"' => -1
//  //      case '}' => 0
//  //      case _ => 1
//  //    }
//    if (k == '"') -1
//    else if (k == '}') 0
//    else 1
//
//  @inline
//  private final def nextChar2(k: Int): Boolean = {
//    if (k == ',' || k == '}' || k == ']' || k == '\n') false
//    else true
//    //    (k: @switch) match {
//    //      case ',' => false
//    //      case '}' => false
//    //      case ']' => false
//    //      case '\n' => false
//    //      case _ => true
//    //    }
//  }
//
//  private final def unroll(json: Array[Byte], pos: Int): Unit = {
//    var j = pos
//    var c = false
//    var index = -1
//    val u = pos + 12
//    var prev: Byte = ' '
//    val res = new Array[Boolean](12)
//    var k = 0
//    while (j < u) {
//      val b = json(j)
//      c = findNextChar(b.toInt, json, prev)
//      res.update(k, c)
//      k = k + 1
//      //logger.debug(s"Current = ${b.toChar} and result = $c")
//      //      val zz = nextChar(b)
//      //      c =
//      //        if (zz == 1) true
//      //        else if (zz == 0) false
//      //        else if (prev == '\\') false else true
//
//      prev = b
//      /*
//            if (!c && index == -1)
//              index = j + 1 // Add 1 for next char
//                */
//      j = j + 1
//    }
//
//    k = 0
//    while (index == -1 && k < res.length) {
//      if (!res(k))
//        index = k + pos
//      k = k + 1
//    }
//
//    //        val t = unrollString(json, pos)
//    //logger.debug(s"Result in index = $index")
//    if (index == -1) {
//      mt.c = true
//      mt.i = j + 1
//    } else {
//      mt.c = false
//      mt.i = index
//    }
//  }
//
//  //  class MutableTuple(var i: Int, var c: Boolean)
//  val mt = Foo.mt
//
//  @tailrec
//  private final def doString(json: Array[Byte], pos: Int, continue: Boolean): Int = {
//    //logger.debug(s"in toString pos = $pos and continue = $continue")
//    if (!continue) pos
//    else {
//      if (json.length - pos >= 16) {
//        Foo.unroll(json, pos)
//        //logger.debug(s"what we got = ${mt.c} and ${mt.i}")
//        if (mt.c) doString(json, mt.i - 1, true)
//        else mt.i
//        /*
//        var j = pos
//        var c = false
//        var index = -1
//        val u = pos + 12
//        var prev: Byte = ' '
//        while (j < u) {
//          val b = json(j)
//          //          c = findNextChar(b, json, prev)
//          val zz = nextChar(b)
//          c =
//            if (zz == 1) true
//            else if (zz == 0) false
//            else if (prev == '\\') false else true
//
//          prev = b
//
//          if (!c && index == -1)
//            index = j
//          j = j + 1
//        }
//
//        //        val t = unrollString(json, pos)
//        if (c) doString(json, j + 1, c)
//        else index
//        */
//
//      } else {
//        doString(json, pos + 1, findNextChar(json(pos).toInt, json, json(pos - 1)))
//      }
//    }
//  }
//
//  sealed trait Result
//  case object Done extends Result
//  case object NotDone extends Result
//
//  //  class State(json: Array[Byte], var position: Int, var prevByte: Byte)
//
//  @tailrec
//  private def doValue(json: Array[Byte], pos: Int, continue: Boolean): Int
//  = {
//    if (!continue) pos
//    else {
//      /*
//            if (json.length - pos >= 8) {
//              var j = pos
//              var c = false
//              var index = -1
//              val u = pos + 8
//              while (j < u) {
//                val b = json(j)
//                c = nextChar2(b)
//                //          val zz = nextChar(b)
//                //          c =
//                //            if (zz == 1) true
//                //            else if (zz == 0) false
//                //            else if (prev == '\\') false else true
//                //          c = (nextChar(b): @switch) match {
//                //            case -1 => if (prev == '\\') false else true
//                //            case 0 => false
//                //            case 1 => true
//                //          }
//
//
//                if (!c && index == -1)
//                  index = j
//                j = j + 1
//              }
//
//              //        val t = unrollString(json, pos)
//              if (c) doValue(json, j + 1, nextChar2(json(j)))
//              else index
//            } else
//            */
//      //      if (json.length - pos >= 32) {
//      //        val t = unrollValue(json, pos)
//      //        if (t._2) doValue(json, t._1 + 1, t._2)
//      //        else t._1f
//      //      } else {
//
//      doValue(json, pos + 1, nextChar2(json(pos)))
//      //      }
//    }
//  }
//
//  private final def parseStartOfValue(json: Array[Byte], pos: Int): Int = {
//    val first = json(pos - 1)
//    val startIndex =
//      if (first == '"') StartIndex(pos - 1)
//      else StartIndex(pos - 1)
//    var localPos = pos
//    val kPrev = json(localPos)
//    //    localPos = localPos + 1
//    //logger.debug(s"first = ${first.toChar} and k = ${kPrev.toChar} w/ localPos = $localPos and ${json(localPos).toChar}")
//
//    localPos =
//      if (first == '"') {
//        // String
//        // Perform read to get next byte after opening
//        val z = json(localPos)
//        doString(json, localPos, findNextChar(z.toInt, json, kPrev))
//      } else {
//        doValue(json, localPos, nextChar2(kPrev))
//      }
//
//    val ll = json(localPos - 1)
//    if (ll == '}') {
//      // At this point, we are two chars ahead of the last char
//      // that we want to track.
//      // Minus 1 to reset cursor to the next thing to look for and
//      // minus 2 to grab correct last index
//      localPos = localPos - 1
//      EndIndex(localPos - 1)
//    } else if (ll == ',') {
//      // At this point, we are two chars ahead of the last char
//      // that we want to track.
//      // Minus 1 to reset cursor to the next thing to look for and
//      // minus 2 to grab correct last index
//      localPos = localPos - 1
//      EndIndex(localPos - 1)
//    } else if (ll == '\n') EndIndex(localPos - 2) // Do not count newline
//    else if (ll == ']') {
//      localPos = localPos - 1
//      EndIndex(localPos - 1)
//    } else EndIndex(localPos - 1)
//    /*
//        val endIndex = (json(localPos - 1): @switch) match {
//          case '}' =>
//            // At this point, we are two chars ahead of the last char
//            // that we want to track.
//            // Minus 1 to reset cursor to the next thing to look for and
//            // minus 2 to grab correct last index
//            localPos = localPos - 1
//            EndIndex(localPos - 1)
//          case ',' =>
//            // At this point, we are two chars ahead of the last char
//            // that we want to track.
//            // Minus 1 to reset cursor to the next thing to look for and
//            // minus 2 to grab correct last index
//            localPos = localPos - 1
//            EndIndex(localPos - 1)
//          case '\n' =>
//            EndIndex(localPos - 2) // Do not count newline
//          case ']' =>
//            localPos = localPos - 1
//            EndIndex(localPos - 1)
//          case _ =>
//            EndIndex(localPos - 1)
//        }
//    */
//    /*
//    val endIndex = if (k == '}' || k == ',') {
//      // At this point, we are two chars ahead of the last char
//      // that we want to track.
//      // Minus 1 to reset cursor to the next thing to look for and
//      // minus 2 to grab correct last index
//      localPos = localPos - 1
//      EndIndex(localPos - 1)
//    } else if (k == '\n') {
//      EndIndex(localPos - 2) // Do not count newline
//    } else if (k == ']') {
//      localPos = localPos - 1
//      EndIndex(localPos - 1)
//    } else
//      EndIndex(localPos - 1)
//*/
//    localPos
//  }
//
//  private final def doStartOfObject[T](s: MutableState[T]): Unit =
//    if (s.cc == '{') {
//      s.s = StartOfKey
//      s.pos = s.pos + 1
//    } else sys.error(s"Unexpected state! x")
//
//  private final def doStartOfKey[T](s: MutableState[T]): Unit =
//    if (s.cc == '"') {
//      val localPosition = parseStartOfKey(s.json, s.pos)
//      s.s = EndOfKey
//      s.pos = localPosition + 1
//    } else if (s.cc == '}') {
//      s.s = EndOfKey
//      s.pos = s.pos + 1
//    }
//    else sys.error(s"Unsupported = ${s.cc} at position = ${s.pos}")
//
//
//  private final def doEndOfKey[T](s: MutableState[T]): Unit =
//    if (s.cc == ':') {
//      s.s = LookingForNextTerm
//      s.pos = s.pos + 1
//    } else sys.error(
//      s"Unexpected state! = [${s.cc.toChar}] at position = ${s.pos}")
//
//  private final def doLookingForNextTerm[T](
//    s: MutableState[T]): Unit =
//    if (s.cc == '{') s.s = StartOfObject
//    else if (s.cc == '[') {
//      s.s = StartOfArray
//      s.pos = s.pos + 1
//    } else {
//      // Making assumption it's a null, boolean, or number
//      // (not whitespace)
//      s.s = StartOfValue
//      s.pos = s.pos + 1
//    }
//
//  private final def doStartOfValue[T](
//    s: MutableState[T]): Unit = {
//    val localPos = parseStartOfValue(s.json, s.pos)
//    s.s = EndOfValue
//    s.pos = localPos
//  }
//
//  private final def doStartOfArrayValue[T](s: MutableState[T]): Unit =
//  // Peek ahead to handle case of empty array
//    if (s.cc == ']') s.s = EndOfArrayValue
//    else {
//      val localPos = parseStartOfValue(s.json, s.pos)
//      s.s = EndOfArrayValue
//      s.pos = localPos
//    }
//
//  private final def doEndOfValue[T](
//    s: MutableState[T]): Unit =
//    if (s.cc == ',') {
//      s.s = StartOfKey
//      s.pos = s.pos + 1
//    } else if (s.cc == '}') {
//      s.s = EndOfObject
//      s.pos = s.pos + 1
//    } else if (s.cc == ']') {
//      // Given "site":{"cat":["IAB1"],"page": "b"
//      // Then the "site" key should not be deleted when moving
//      // from IAB1 to page
//
//      s.s = EndOfArray
//      s.pos = s.pos + 1
//    } else sys.error(s"Unsupported state = ${s.cc.toChar} at position = ${s.pos}")
//
//  private final def doEndOfArrayValue[T](s: MutableState[T]): Unit =
//    if (s.cc == ',') {
//      s.s = StartOfArrayValue
//      s.pos = s.pos + 1
//    } else if (s.cc == ']') {
//      s.s = EndOfArray
//      s.pos = s.pos + 1
//    } else sys.error(s"Unsupported state = x at position = ${s.pos}")
//
//  private final def doEndOfObject[T](
//    s: MutableState[T]): Unit
//  =
//    if (s.cc == ',') {
//      s.s = LookingForNextFieldAfterEndOfObject
//      s.pos = s.pos + 1
//    } else if (s.cc == '}') {
//      // Nested end of objects could happen inside an array. e.g.:
//      // {{"banner":{"w":740,"h":30}} , {"banner":{"w":320,"h":240}}]
//      s.s = EndOfObject
//      s.pos = s.pos + 1
//    } else if (s.cc == ']') {
//      s.s = EndOfArray
//      s.pos = s.pos + 1
//    } else
//      sys.error(s"Unsupported state = x at position = ${s.pos}")
//
//
//  private final def doLookingForNextFieldAfterEndOfObject[T](
//    s: MutableState[T]): Unit
//  =
//    if (s.cc == '"') s.s = StartOfKey
//    else if (s.cc == '{') s.s = StartOfObject
//    else sys.error(s"Unsupported state = x at position = ${s.pos}")
//
//  private final def doStartOfArray[T](
//    s: MutableState[T]): Unit
//  =
//    if (s.cc == '{') s.s = StartOfObject
//    else s.s = StartOfArrayValue // Assume it's an array of values
//
//  private final def doEndOfArray[T](
//    s: MutableState[T]): Unit
//  =
//    if (s.cc == ',') {
//      // Given: {"imp":[{"id":"1"}],"page":"http://google.com"}
//      // Want to remove 'imp' as key when reaching "}]"
//      s.s = StartOfKey
//      s.pos = s.pos + 1
//    } else if (s.cc == '}') {
//      s.s = EndOfObject
//      s.pos = s.pos + 1
//    } else sys.error(s"Unsupported state = x at position = ${s.pos}")
//
//
//  //  private final def switchOnLabel3[T](s: MutableState[T]): T =
//  //    (s.s.label: @switch) match {
//  //      case 11 => doEndOfArrayValue(s)
//  //      case 5 => doEndOfObject(s)
//  //      case 9 => doLookingForNextFieldAfterEndOfObject(s)
//  //      case 6 => doStartOfArray(s)
//  //      case 7 => doEndOfArray(s)
//  //      case _ => sys.error(s"no way x")
//  //    }
//
//  //  private final def switchOnLabel2[T](s: MutableState[T]): T
//  //  =
//  //    (s.s.label: @switch) match {
//  //      case 8 => doLookingForNextTerm(s)
//  //      case 10 => doStartOfArrayValue(s)
//  //      case 0 => doStartOfObject(s)
//  //      case _ => switchOnLabel3(s)
//  //    }
//
//  val tokenIndexes: Array[Int] = new Array(1200)
//  val tokenIndexes2: Array[Int] = new Array(256)
//  private val har = {
//    val z = Array.fill[Int](256)(0)
//    z.update('"'.toInt, 1)
//    z.update('}'.toInt, 1)
//    z.update('{'.toInt, 1)
//    z.update('}'.toInt, 1)
//    z.update('['.toInt, 1)
//    z.update(']'.toInt, 1)
//    z
//  }
//
//  val small: Array[Byte] = new Array(8)
//  def tryItOut(json: Array[Byte]): Long = {
//
//    var i = 0
//    var j = 0
//    var sum = 0
//    //    System.arraycopy(json, 0, tokenIndexes, 0, json.length)
//    val splits = json.length - (json.length % 1)
//    while (i < splits - 3) {
//      if (har(json.apply(i)) == 1) {
//        sum = sum + 1
//
//      } else sum = sum - 1
//      i = i + 1
//
//      if (har(json.apply(i)) == 1) {
//        sum = sum + 1
//
//      } else sum = sum - 1
//      i = i + 1
//
//      if (har(json.apply(i)) == 1) {
//        sum = sum + 1
//
//      } else sum = sum - 1
//      i = i + 1
//
//      if (har(json.apply(i)) == 1) {
//        sum = sum + 1
//
//      } else sum = sum - 1
//      i = i + 1
//
//      //      println(z ^ '"')
//      //      if (z == '"') {
//      //      if (har(z) != 0) {
//
//      //        j = j + 1
//      //      }
//      //      j = j + har(z.toInt)
//
//    }
//
//    //    while (i < 256) {
//    //      tokenIndexes2.update(j, i)
//    //      j = j + tokenIndexes(i)
//    //      i = i + 1
//    //    }
//
//    sum
//    /*
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//
//          z = json(i)
//          if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//            //      tokenIndexes.update(j, i)
//            j = j + 1
//          }
//          //      j = j + har(z.toInt)
//          i = i + 1
//        }
//    */
//    /*
//    while (i < json.length) {
//      val z = json(i)
//
//
////      if (z == '{' || z == '"' || z == '}' || z == '[' || z == ']') {
//        tokenIndexes.update(j, i)
//        j = j + har(z.toInt)
////      }
//      i = i + 1
//    }
//    */
//  }
//
//  private val jumpTable: Array[MutableState[_] => Unit] = {
//    val z = new Array[MutableState[_] => Unit](12)
//    z.update(0, s => doStartOfObject(s))
//    z.update(1, s => doStartOfKey(s))
//    z.update(2, s => doEndOfKey(s))
//    z.update(3, s => doStartOfValue(s))
//    z.update(4, s => doEndOfValue(s))
//    z.update(5, s => doEndOfObject(s))
//    z.update(6, s => doStartOfArray(s))
//    z.update(7, s => doEndOfArray(s))
//    z.update(8, s => doLookingForNextTerm(s))
//    z.update(9, s => doLookingForNextFieldAfterEndOfObject(s))
//    z.update(10, s => doStartOfArrayValue(s))
//    z.update(11, s => doEndOfArrayValue(s))
//    z
//  }
//  private final def switchOnLabel[T](s: MutableState[T]): Unit
//  = {
//    //logger.debug(s"Switching on ${s.s}")
//    s.s.doit(s)
//
//    //    (s.s.label: @switch) match {
//    //      case 1 => doStartOfKey(s)
//    //      case 2 => doEndOfKey(s)
//    //      case 3 => doStartOfValue(s)
//    //      case 4 => doEndOfValue(s)
//    //      case 8 => doLookingForNextTerm(s)
//    //      case 10 => doStartOfArrayValue(s)
//    //      case 0 => doStartOfObject(s)
//    //      case 11 => doEndOfArrayValue(s)
//    //      case 5 => doEndOfObject(s)
//    //      case 9 => doLookingForNextFieldAfterEndOfObject(s)
//    //      case 6 => doStartOfArray(s)
//    //      case 7 => doEndOfArray(s)
//    //      case _ => switchOnLabel2(s)
//    //    }
//  }
//
//
//  private final def doParse[T](s: MutableState[T]): T
//  = {
//    while (s.pos < s.json.length) {
//      val z = s.json(s.pos)
//      //      if (z == ' ' || z == '\t' || z == '\n' || z == '\r') s.pos = s.pos + 1
//      //      else switchOnLabel({
//      //        s.cc = z
//      //        s
//      //    }
//      //    )
//      (z) match {
//        case ' ' => s.pos = s.pos + 1
//        case '\t' => s.pos = s.pos + 1
//        case '\n' => s.pos = s.pos + 1
//        case '\r' => s.pos = s.pos + 1
//        case cc =>
//          //          jumpTable(s.s.label)({
//          switchOnLabel({
//            s.cc = cc
//            s
//          })
//      }
//    }
//
//    s.t
//  }
//
//}
