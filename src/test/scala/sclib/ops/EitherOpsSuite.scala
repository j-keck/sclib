package sclib.ops

import org.scalatest.{FunSuite, Matchers}

class EitherOpsSuite extends FunSuite with Matchers {

  test("sequence with Right values"){
    val l: List[Either[Nothing, Int]] = List.range(1, 10).map(Right.apply)
    val e: Either[Nothing, List[Int]] = EitherOps.sequence(l)
    e should be(Right(List.range(1, 10)))
  }

  test("sequence with one Left"){
    val l = List.range(1, 10).map(Right.apply).updated(6, Left("BOOM"))
    val e: Either[String, List[Int]] = EitherOps.sequence(l)
    e should be(Left("BOOM"))
  }

}
