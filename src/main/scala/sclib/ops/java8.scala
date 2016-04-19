package sclib.ops

import java.util.function.{BinaryOperator, Consumer, Predicate, Function => JFunction}
import java.util.stream.{Stream => JStream}

import scala.collection.JavaConverters

object java8 extends java8
/**
  * java8 interoperability
  *
  * ''check the member documentation for examples''
  */
trait java8 {

  import scala.language.implicitConversions

  /**
    * extend `Stream`
    */
  implicit class JStreamOps[A](js: JStream[A]) {

    /**
      * create a `scala.collection.Iterator` from a `java.util.stream.Stream`
      *
      * @example
      * {{{
      *  scala> import sclib.ops.java8._
      *  scala> java.util.stream.Stream.of(1, 2, 3, 4).toIterator
      *  res0: Iterator[Int] = non-empty iterator
      * }}}
      */
    def toIterator: Iterator[A] =
      JavaConverters.asScalaIteratorConverter(js.iterator()).asScala


    /**
      * create a `scala.collection.immutable.List` from a `java.util.stream.Stream`
      *
      * @example
      * {{{
      *  scala> import sclib.ops.java8._
      *  scala> java.util.stream.Stream.of(1, 2, 3, 4).toList
      *  res0: List[Int] = List(1, 2, 3, 4)
      * }}}
      */
    def toList: List[A] = toIterator.toList
  }

  /**
    * convert a `scala.Function1` to a `java.util.function.Function`
    *
    * @example
    *
    * '''without''' the import:
    * {{{
    * scala> java.util.stream.Stream.of(1, 2, 3,4).map((_: Int) * 10).toArray
    * <console>:12: error: type mismatch;
    * found   : Int => Int
    * required: java.util.function.Function[_ >: Int, _]
    *        java.util.stream.Stream.of(1, 2, 3,4).map((_: Int) * 10).toArray
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.stream.Stream.of(1, 2, 3,4).map((_: Int) * 10).toArray
    * res1: Array[Object] = Array(10, 20, 30, 40)
    * }}}
    */
  implicit def SFunction2JFunction[A, B](f: A => B) = new JFunction[A, B] {
    override def apply(t: A): B = f(t)
  }


  /**
    * convert a `scala.Function1` to a `java.util.function.Predicate`
    *
    * @example
    *
    * '''without''' the import:
    *
    * {{{
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).filter((_: Int) < 3).toArray
    * <console>:12: error: type mismatch;
    *  found   : Int => Boolean
    *  required: java.util.function.Predicate[_ >: Int]
    *        java.util.stream.Stream.of(1, 2, 3, 4).filter((_: Int) < 3).toArray
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).filter((_: Int) < 3).toArray
    * res1: Array[Object] = Array(1, 2)
    * }}}
    */
  implicit def SFunction2Predicate[A](f: A => Boolean) = new Predicate[A] {
    override def test(t: A): Boolean = f(t)
  }

  /**
    * convert a `scala.Function1` to a `java.util.function.Consumer`
    *
    * @example
    *
    * '''without''' the import:
    *
    * {{{
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).forEach(println(_: Int))
    * <console>:12: error: type mismatch;
    *  found   : Int => Unit
    *  required: java.util.function.Consumer[_ >: Int]
    *        java.util.stream.Stream.of(1, 2, 3, 4).forEach(println(_: Int))
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).forEach(println(_: Int))
    * 1
    * 2
    * 3
    * 4
    * }}}
    */
  implicit def SFunction2Consumer[A](f: A => Unit) = new Consumer[A] {
    override def accept(t: A): Unit = f(t)
  }

  /**
    * convert a `scala.Function2` to a `java.util.function.BinaryOperator`
    *
    * @example
    *
    * '''without''' the import:
    * {{{
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).reduce(0, (_: Int) + (_: Int))
    * <console>:12: error: overloaded method value reduce with alternatives:
    *   [U](x\$1: U, x\$2: java.util.function.BiFunction[U, _ >: Int, U], x\$3: java.util.function.BinaryOperator[U])U <and>
    *   (x\$1: java.util.function.BinaryOperator[Int])java.util.Optional[Int] <and>
    *   (x\$1: Int,x\$2: java.util.function.BinaryOperator[Int])Int
    *  cannot be applied to (Int, (Int, Int) => Int)
    *        java.util.stream.Stream.of(1, 2, 3, 4).reduce(0, (_: Int) + (_: Int))
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.stream.Stream.of(1, 2, 3, 4).reduce(0, (_: Int) + (_: Int))
    * res1: Int = 10
    * }}}
    */
  implicit def SFunction2BinaryOperation[A](f: (A, A) => A) = new BinaryOperator[A] {
    override def apply(t: A, u: A): A = f(t, u)
  }
}
