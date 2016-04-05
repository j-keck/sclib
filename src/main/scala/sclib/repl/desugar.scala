package sclib.repl

object desugar {

  import scala.reflect.macros.blackbox.Context
  import scala.reflect.runtime.universe._
  import scala.language.experimental.macros

  def apply(expr: Any): Unit = macro desugar

  def desugar(c: Context)(expr: c.Expr[Any]): c.Expr[Unit] = {
    import c.universe._
    println(show(expr.tree))
    reify {}
  }

}
