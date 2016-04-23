package sclib.io.fs

import org.scalatest.{FunSuite, Matchers}

class FSEntrySuite extends FunSuite with Matchers {


  test("depth"){

    def check(a: String, b: String, expected: Int) = withClue(s"'$a' vs '$b'"){
      dir(a).get.depth(dir(b).get) should be(expected)
    }

    check("/", "/", 0)
    check("/", "/tmp", 1)
    check("/tmp", "/", 1)
    check("/", "/tmp/a/b", 3)
    check("/tmp/a/b/c", "/tmp/a/../a/x/../b/c", 0)
  }
}
