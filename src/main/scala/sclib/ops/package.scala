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
  object all
      extends either
      with int
      with java8
      with list
      with option
      with ordering
      with string
      with `try`
}
