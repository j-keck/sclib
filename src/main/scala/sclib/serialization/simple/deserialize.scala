package sclib.serialization.simple

import sclib.z.StateT
import sclib.ops.all._

trait deserialize {

  type DeserializeState[A] = StateT[Either[String, ?], String, A]

  trait Deserialize[A] {
    def apply: DeserializeState[A]

    protected def next: DeserializeState[String] =
      StateT[Either[String, ?], String, String](
          (_: String).split(":", 2) match {
        case Array(l, str) => l.toIntE.map(str.splitAt)
        case Array("") =>
          "unable to deserialize empty string - expected: '<LENGTH>:<CONTENT>...'".left
        case Array(x) =>
          s"unable to deserialize invalid string - expected: '<LENGTH>:<CONTENT>...', actual: '${x}'".left
      })
  }

  object Deserialize {
    def apply[A: Deserialize](s: String): Either[String, A] =
      Deserialize.apply[A].eval(s)

    def apply[A: Deserialize]: DeserializeState[A] =
      implicitly[Deserialize[A]].apply
  }

  //
  // primitives
  //

  implicit val stringDes = new Deserialize[String] {
    override def apply: DeserializeState[String] = next
  }

  // FIXME: if i use 'implicit val xxxDes ..', the compiler throws: 'not found: type $anon'
  implicit def charDes = new Deserialize[Char] {
    override def apply: DeserializeState[Char] = next.flatMapF(_.toCharE)
  }

  implicit def intDes = new Deserialize[Int] {
    override def apply: DeserializeState[Int] = next.flatMapF(_.toIntE)
  }

  implicit def longDes = new Deserialize[Long] {
    override def apply: DeserializeState[Long] = next.flatMapF(_.toLongE)
  }

  implicit def doubleDes = new Deserialize[Double] {
    override def apply: DeserializeState[Double] = next.flatMapF(_.toDoubleE)
  }

  implicit def booleanDes = new Deserialize[Boolean] {
    override def apply: DeserializeState[Boolean] = next.flatMapF(_.toBooleanE)
  }

  //
  // container
  //

  implicit def optionDes[A: Deserialize] = new Deserialize[Option[A]] {
    override def apply: DeserializeState[Option[A]] = next.flatMapF {
      case "N" => none.right
      case s   => Deserialize[A](s).map(_.some)
    }
  }

  implicit def eitherDes[A: Deserialize, B: Deserialize] =
    new Deserialize[Either[A, B]] {
      override def apply: DeserializeState[Either[A, B]] =
        next.flatMapF(
            _.splitAt(1) match {
          case ("L", s) => Deserialize[A](s).map(_.left)
          case ("R", s) => Deserialize[B](s).map(_.right)
          case (x, r) =>
            s"unable to deserialize Either: expected 'L' or 'R' prefix, found: '${x}' - in: '${x + r}'".left
        })
    }

  implicit def tupleDes[A: Deserialize, B: Deserialize] =
    new Deserialize[(A, B)] {
      override def apply: DeserializeState[(A, B)] =
        for {
          a <- Deserialize[A]
          b <- Deserialize[B]
        } yield (a, b)
    }

  implicit def listDes[A: Deserialize] = new Deserialize[List[A]] {

    override def apply: DeserializeState[List[A]] = next.flatMapF(unspool)

    // FIXME: implement unspool with unfold?
    private def unspool(s: String): Either[String, List[A]] =
      Deserialize[A].run(s).flatMap {
        case (v, "") => List(v).right
        case (v, xs) => unspool(xs).map(v :: _)
      }
  }

  implicit def vectorDes[A: Deserialize] = new Deserialize[Vector[A]] {

    override def apply: DeserializeState[Vector[A]] = next.flatMapF(unspool)

    private def unspool(s: String): Either[String, Vector[A]] =
      Deserialize[A].run(s).flatMap {
        case (v, "") => Vector(v).right
        case (v, xs) => unspool(xs).map(v +: _)
      }
  }

  implicit def setDes[A: Deserialize] = new Deserialize[Set[A]] {

    override def apply: DeserializeState[Set[A]] = next.flatMapF(unspool)

    private def unspool(s: String): Either[String, Set[A]] =
      Deserialize[A].run(s).flatMap {
        case (v, "") => Set(v).right
        case (v, xs) => unspool(xs).map(_ + v)
      }
  }

  implicit def mapDes[A: Deserialize, B: Deserialize] =
    new Deserialize[Map[A, B]] {
      override def apply: DeserializeState[Map[A, B]] = next.flatMapF(unspool)

      private def unspool(s: String): Either[String, Map[A, B]] =
        Deserialize[Tuple2[A, B]].run(s).flatMap {
          case ((a, b), "") => Map(a                  -> b).right
          case ((a, b), xs) => unspool(xs).map(_ + (a -> b))
        }
    }
}
