package sclib.ops

import java.util.stream.{Stream => JStream}
import java.util.function.{Predicate, Function => JFunction}

import scala.collection.JavaConverters

/**
  * java8 interoperability
  *
  *  - `java.util.stream.Stream` extensions: [[sclib.ops.java8.JStreamOps]]
  *  - create a `java.util.function.Function` from a `scala.Function1`: [[sclib.ops.java8.SFunction2JFunction]]
  *  - create a `java.util.function.Predicate` from a `scala.Function1`: [[sclib.ops.java8.SFunction2Predicate]]
  *
  *  ''check the member documentation for examples''
  */
object java8 {
  import scala.language.implicitConversions

  /**
    * extend `Stream`
    */
  implicit class JStreamOps[A](js: JStream[A]) {

    /**
      * create a `Iterator` from a `java.util.stream.Stream`
      *
      * @example {{{
      * scala> import sclib.ops.java8._
      * scala> java.util.Arrays.asList(1, 2, 3, 4).stream.toIterator
      * res0: Iterator[Int] = non-empty iterator
      * }}}
      */
    def toIterator: Iterator[A] =
      JavaConverters.asScalaIteratorConverter(js.iterator()).asScala


    /**
      * create a `collection.immutable.List` from a `java.util.stream.Stream`
      *
      * @example {{{
      * scala> import sclib.ops.java8._
      * scala> java.util.Arrays.asList(1, 2, 3, 4).stream.toList
      * res0: List[Int] = List(1, 2, 3, 4)
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
    * scala> java.util.Arrays.asList(1, 2, 3, 4).stream().map((_: Int) * 10).toArray
    * <console>:12: error: type mismatch;
    * found   : Int => Int
    * required: java.util.function.Function[_ >: Int, _]
    * java.util.Arrays.asList(1, 2, 3, 4).stream().map((_: Int) * 10).toArray
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.Arrays.asList(1, 2, 3, 4).stream().map((_: Int) * 10).toArray
    * res1: Array[Object] = Array(10, 20, 30, 40)
    * }}}
    */
  implicit def SFunction2JFunction[A, B](f: A => B) = new JFunction[A, B]{
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
    * scala> java.util.Arrays.asList(1, 2, 3, 4).stream().filter((_:Int) < 3).toArray
    * <console>:12: error: type mismatch;
    * found   : Int => Boolean
    * required: java.util.function.Predicate[_ >: Int]
    *        java.util.Arrays.asList(1, 2, 3, 4).stream().filter((_:Int) < 3).toArray
    * }}}
    *
    * '''with''' the import:
    * {{{
    * scala> import sclib.ops.java8._
    * scala> java.util.Arrays.asList(1, 2, 3, 4).stream().filter((_:Int) < 3).toArray
    * res1: Array[Object] = Array(1, 2)
    * }}}
    */
  implicit def SFunction2Predicate[A](f: A => Boolean) = new Predicate[A] {
    override def test(t: A): Boolean = f(t)
  }
}
