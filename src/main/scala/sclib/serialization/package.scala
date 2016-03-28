package sclib

/**
  * simple serialization / deserialization
  *
  * example:
  * {{{
  * scala> import sclib.serialization._
  * scala> val s = Serialize(1 to 20 toList)
  * s: String = 71:1:11:21:31:41:51:61:71:81:92:102:112:122:132:142:152:162:172:182:192:20
  * scala> Deserialize[List[Int]](s)
  * res0: List[Int] = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
  * }}}
  */
package object serialization extends serialize with deserialize