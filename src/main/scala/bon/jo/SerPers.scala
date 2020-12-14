package bon.jo

import java.io._
import java.nio.file.Paths

import scala.util.{Failure, Success, Try}


trait SerPers[A] {
  def save(e: A): Unit

  def restore(e: A):  Option[A]
}
/**
 * Allow, by importing implicit member of this class to do [[SerPers.SerPersObject.save]]
 * and  [[SerPers.SerPersObject.restore]] on any object, by default, the persistance is done by serialization
 * but can be overloaded.
 */
object SerPers {


  /**
   *
   * @tparam A the type
   */
  trait SerPersObject[A] {
    /**
     * save the object
     */
    def save(): Unit

    /**
     * restore the object
     * @return
     */
    def restore(): Option[A]
  }

  /**
   * Identify an object with a [[scala.Predef.String]]
   * @tparam A the type
   */
  trait IdString[A] {
    /**
     *
     * @param a : the object to identify
     * @return the id [[scala.Predef.String]]
     */
    def read(a: A): String
  }

  implicit class SerObject[A: SerPers](a: A) extends SerPersObject[A] {
    type i = SerPers[A]

    override def save(): Unit = implicitly[i].save(a)

    override def restore():  Option[A] = implicitly[i].restore(a)
  }

  object ImplicitDef{
    implicit def create[A: IdString] : SerPers[A] = {
       new SerPersImpl[A]
    }
  }
  class SerPersImpl[A: IdString] extends SerPers[A] {
    type id = IdString[A]

    private def in(inp: InputStream) = new ObjectInputStream(new BufferedInputStream(inp))

    private def out = {
      val outBuff = new ByteArrayOutputStream()
      (new ObjectOutputStream(outBuff), outBuff)
    }

    private def fileid(a: A) = {
      s"${a.getClass}${implicitly[id].read(a)}.ser"
    }

    private def filteOut(a: A) = new FileOutputStream(fileid(a))

    private def filteInt(a: A) = new FileInputStream(fileid(a))
    override def save(e: A): Unit = {
      val (outp, ouIn) = out
      val fileOut = filteOut(e)
      Try(outp.writeObject(e)).flatMap {
        _ =>
          Try(fileOut.write(ouIn.toByteArray))
      } match {
        case _ =>
          outp.close()
          fileOut.close()
      }
    }

    override def restore(e: A):  Option[A] = {
      if(Paths.get(fileid(e)).toFile.exists()){
        val out = in(filteInt(e))
        val tr =  Try(out.readObject())
        out.close()
        tr match {
          case Failure(exception) =>
            throw exception
          case Success(value) => Some(value.asInstanceOf[A])
        }
      }else{
        None
      }

    }
  }

}
