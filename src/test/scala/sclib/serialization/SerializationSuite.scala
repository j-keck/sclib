package sclib.serialization

import org.scalatest.{FunSuite, Matchers}
import sclib.ops.either._
import sclib.serialization.simple._

class SerializationSuite extends FunSuite with Matchers {

  //
  // serialize
  //

  test("serialize string") {
    Serialize("44:one string") should be("13:44:one string")
  }

  test("serialize char") {
    Serialize('x') should be("1:x")
  }

  test("serialize int") {
    Serialize(12345) should be("5:12345")
  }

  test("serialize boolean") {
    Serialize(true) should be("4:true")
  }

  test("serialize list of int") {
    Serialize(List(1, 5, 2)) should be("9:1:11:51:2")
  }

  test("serialize vector of int") {
    Serialize(Vector(1, 5, 2)) should be("9:1:11:51:2")
  }

  test("serialize option") {
    Serialize(Option.empty[Int]) should be("1:N")
    Serialize(Option("N")) should be("3:1:N")
  }

  test("serialize either") {
    Serialize("left".left[Int]) should be("7:L4:left")
    Serialize("right".right[Int]) should be("8:R5:right")
  }

  //
  // deserialize
  //

  test("deserialize invalid input") {
    Deserialize[String]("BOOM") should be(
      "unable to deserialize invalid string - expected: '<LENGTH>:<CONTENT>...', actual: 'BOOM'".left)
    Deserialize[String]("") should be(
      "unable to deserialize empty string - expected: '<LENGTH>:<CONTENT>...'".left)
  }

  test("deserialize string") {
    Deserialize[String]("4:abcd") should be("abcd".right)
  }

  test("deserialize char") {
    Deserialize[Char]("1:x") should be('x'.right)
    Deserialize[Char]("2:xx") should be("'xx' contains more than a char".left)
    Deserialize[Char]("0:") should be(
      "empty string doesn't contain any char".left)
  }

  test("deserialize int") {
    Deserialize[Int]("6:123456") should be(123456.right)
  }

  test("deserialize boolean") {
    Deserialize[Boolean]("5:false") should be(false.right)
  }

  test("deserialize list of int") {
    Deserialize[List[Int]]("9:1:11:51:2") should be(List(1, 5, 2).right)
  }

  test("deserialize vector of int") {
    Deserialize[Vector[Int]]("9:1:11:51:2") should be(Vector(1, 5, 2).right)
  }

  test("deserialize option") {
    Deserialize[Option[String]]("1:N") should be(None.right)
    Deserialize[Option[String]]("3:1:N") should be(Some("N").right)
  }

  test("deserialize either") {
    // 'xxx.right' are not usable here, because we are already on a 'Either', which has a '.right'
    // function to get the 'RightProjection'
    Deserialize[Either[String, Int]]("7:L4:left") should be(
      Right("left".left[Int]))
    Deserialize[Either[Int, String]]("8:R5:right") should be(
      Right("right".right[Int]))

    Deserialize[Either[Int, Int]]("4:X1:1") should be(
      "unable to deserialize Either: expected 'L' or 'R' prefix, found: 'X' - in: 'X1:1'".left)
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
    override def apply: DeserializeState[C] = for {
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
    Deserialize[List[C]](csStr) should be(cs.right)
  }

}
