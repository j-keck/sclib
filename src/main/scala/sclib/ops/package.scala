package sclib

/**
  * stdlib extensions
  *
  * ''check the member documentation for examples''
  */
package object ops {

  /**
    * use `import sclib.ops.all._` to import all
    */
  object all extends either with java8 with list with option with string with `try`
}
