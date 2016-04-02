package sclib.dp

import org.scalatest.{FunSuite, Matchers}
import sclib.ops.either._

class AppFSuite extends FunSuite with Matchers {

  case class Config(a: Int, crash: Boolean)

  private def job1 = AppF[Config, String, String]{ cfg =>
    "in job1".right
  }

  private def job2 = AppF[Config, String, Int]{ cfg =>
    if(cfg.crash) "crash in job2".left else cfg.a.right
  }

  private val action = for {
    a <- job1
    b <- job2
    c <- AppF{cfg: Config => s"config value a is: '${cfg.a}'".right[String]}
  } yield (a, b, c)


  test("successful action"){
    action.runEitherT.runReader(Config(99, false)) should be(("in job1", 99, "config value a is: '99'").right)
  }

  test("failed action"){
    action.runEitherT.runReader(Config(99, true)) should be("crash in job2".left)
  }

  test("lift"){
    val action = for{
      a <- AppF{i: Int => i.right[String]}
      b <- AppF.lift(44.right[String])
    } yield (a, b)

    action.runEitherT.runReader(3) should be((3, 44).right)
  }

}
