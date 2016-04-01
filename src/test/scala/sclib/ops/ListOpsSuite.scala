package sclib.ops

import org.scalatest.{FunSuite, Matchers}
import sclib.ops.list._

class ListOpsSuite extends FunSuite with Matchers {

  test("unfoldRight") {
    ListOps.unfoldRight(0) { i =>
      if (i > 5) None else Some((i, i + 1))
    } should be(List(0, 1, 2, 3, 4, 5))
  }

  test("unfoldLeft") {
    ListOps.unfoldLeft(0) { i =>
      if (i > 5) None else Some((i + 1, i))
    } should be(List(5, 4, 3, 2, 1, 0))
  }
}
