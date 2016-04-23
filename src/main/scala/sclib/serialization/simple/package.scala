package sclib.serialization

import sclib.io.fs.Writable

/**
  * simple serialization / deserialization
  *
  * @example
  * {{{
  * scala> import sclib.serialization.simple._
  * scala> val s = Serialize(List.range(1, 20))
  * s: String = 71:1:11:21:31:41:51:61:71:81:92:102:112:122:132:142:152:162:172:182:192:20
  * scala> Deserialize[List[Int]](s)
  * res0: Either[String,List[Int]] = Right(List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19))
  * }}}
  */
package object simple extends serialize with deserialize{

  implicit def serializeWritable[A: Serialize] = new Writable[A] {
    override def apply(a: A): Iterator[String] = Iterator.single(Serialize(a))
  }
}