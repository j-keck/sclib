package sclib.ops

import org.scalatest.{FunSuite, Matchers}

class EitherOpsSuite extends FunSuite with Matchers {
  import sclib.ops.either._

  test("sequence with Right values"){
    val l: List[Either[String, Int]] = List.range(1, 10).map(_.right[String])
    val e: Either[String, List[Int]] = l.sequence
    e should be(Right(List.range(1, 10)))
  }

  test("sequence with one Left"){
    val l: List[Either[String, Int]] = List.range(1, 10).map(Right.apply).updated(6, Left("BOOM"))
    val e: Either[String, Seq[Int]] = l.sequence
    e should be(Left("BOOM"))
  }


  test("right biased map / flatMap"){
    val res1 = for {
      a <- 1.right
      b <- "BOOM".left[Int]
    } yield a + b
    res1 should be(Left("BOOM"))

    val res2 = for{
      a <- 1.right
      b <- 2.right
    } yield a + b
    res2 should be(Right(3))
  }

}
