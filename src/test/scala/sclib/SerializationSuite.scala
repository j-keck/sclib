package sclib

import org.scalatest.{FunSuite, Matchers}

class SerializationSuite extends FunSuite with Matchers {
  import sclib.serialization._

  //
  // serialize
  //

  test("serialize string") {
    Serialize("44:one string") should be("13:44:one string")
  }

  test("serialize int") {
    Serialize(12345) should be("5:12345")
  }

  test("serialize boolean") {
    Serialize(true) should be("4:true")
  }

  test("serialize vector of int") {
    Serialize(Vector(1, 5, 2)) should be("9:1:11:51:2")
  }

  //
  // deserialize
  //

  test("deserialize string") {
    Deserialize[String]("4:abcd") should be("abcd")
  }

  test("deserialize int") {
    Deserialize[Int]("6:123456") should be(123456)
  }

  test("deserialize boolean") {
    Deserialize[Boolean]("5:false") should be(false)
  }

  test("deserialize vector of int") {
    Deserialize[Vector[Int]]("9:1:11:51:2") should be(Vector(1, 5, 2))
  }


  //
  // serialize / deserialize case class
  //
  case class C(a: Int, b: String, c: List[Long], d: (Int, String), e: Boolean)

  val cs = List(
    C(43, "a string", List(433L, 6534L, 1243444L), 123 -> "abc", false),
    C(34, "b string", List(3L), 321 -> "cba", true)
  )

  val csStr = "89:2:438:a string20:3:4334:65347:12434443:1233:abc5:false2:348:b string3:1:33:3213:cba4:true"

  implicit val cSer = new Serialize[C] {
    override def apply(a: C): String = a match {
      case C(a, b, c, d, e) => Serialize(a) + Serialize(b) + Serialize(c) + Serialize(d) + Serialize(e)
    }
  }

  implicit val cDes = new Deserialize[C] {
    override def apply: State[String, C] = for {
      a <- Deserialize[Int]
      b <- Deserialize[String]
      c <- Deserialize[List[Long]]
      d <- Deserialize[(Int, String)]
      e <- Deserialize[Boolean]
    } yield C(a, b, c, d, e)
  }

  test("serialize case class") {
    Serialize(cs) should be(csStr)
  }

  test("deserialize case class") {
    Deserialize[List[C]](csStr) should be(cs)
  }
}
