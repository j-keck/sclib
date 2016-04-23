package sclib.io.fs

import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission._

import org.scalatest.{FunSuite, Matchers}
import sclib.ops.all._

class FSPermSuite extends FunSuite with Matchers {
  import FSPerm._

  test("calc: valid") {
    calc(731) should be(
        Seq(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_WRITE, GROUP_EXECUTE, OTHERS_EXECUTE).success)
  }

  test("calc: invalid"){
    calc(738).fold(_.getMessage)(_ => "Failure expected!") should be("Invalid file mode: 738")
  }


  test("mod: valid"){
    def check(s1: Seq[PosixFilePermission], mode: String, s2: Seq[PosixFilePermission]) = {
      mod(s1, mode).map(_.sorted) should be(s2.sorted.success)
    }

    check(Seq(OWNER_READ), "a+x", Seq(OWNER_READ, OWNER_EXECUTE, GROUP_EXECUTE, OTHERS_EXECUTE))
    check(Seq(OWNER_READ), "a+r", Seq(OWNER_READ, GROUP_READ, OTHERS_READ))

    check(Seq(OWNER_READ, GROUP_READ), "u+wx", Seq(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE, GROUP_READ))

    check(Seq(OWNER_EXECUTE, GROUP_EXECUTE), "u=rw", Seq(OWNER_READ, OWNER_WRITE, GROUP_EXECUTE))

    check(Seq(), "u+r,g+w,o+x", Seq(OWNER_READ, GROUP_WRITE, OTHERS_EXECUTE))

  }

  test("mod: invalid"){
    def check(mode: String, expected: String) = {
      mod(Seq(), mode).fold(_.getMessage)(x => s"Failure expected for mode: ${mode} - received: ${x}") should be(expected)
    }

    check("arwx", "operator [+|-|=] not found")
    check("a/rwx", "operator [+|-|=] not found")
    check("+rwx", "who ([a|u|g|o]+) not found")
    check("a+", "perm ([r|w|x]+) not found")
  }
}
