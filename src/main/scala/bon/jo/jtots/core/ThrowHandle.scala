package bon.jo.jtots.core

object ThrowHandle {
  def noThrow[A](a: => A, default: => A)(msg: Throwable => String): A = {
    try {
      a
    } catch {
      case e: Throwable => {
        println(msg(e));
        default
      }
    }
  }
}
