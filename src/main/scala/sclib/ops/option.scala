package sclib.ops

/**
  * `Option` extensions
  *
  * ''check the member documentation for examples''
  */
object option {

  /**
    * shorthand constructor for `None`
    **/
  def none[A]: Option[A] = None


  /**
    * shorthand constructor for `Some`
    */
  implicit class Any2Some[A](a: A) {
    def some: Option[A] = Some(a)
  }

}
