package bon.jo.jtots.core

import java.io._
import java.nio.file.Paths

import bon.jo.jtots.config.AllConfig.AppDir

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
     def create[A: IdString](implicit appDir: AppDir) : SerPers[A] = {
       new SerPersImpl[A]
    }
  }
  class SerPersImpl[A: IdString](implicit appDir: AppDir) extends SerPers[A] {
    type Id = IdString[A]

    private def in(inp: InputStream) = new ObjectInputStream(new BufferedInputStream(inp))

    private def out = {
      val outBuff = new ByteArrayOutputStream()
      (new ObjectOutputStream(outBuff), outBuff)
    }

    private def fileid(a: A) = {
      s"${a.getClass}${implicitly[Id].read(a)}.ser"
    }
    private def filePath(a: A) = {
      Paths.get(appDir.dirOut).resolve(fileid(a)).toFile
    }

    private def filteOut(a: A) = new FileOutputStream(filePath(a))

    private def filteInt(a: A) = new FileInputStream(filePath(a))
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

    override def restore(a: A):  Option[A] = {
     // println("ffffffffffffffffffffffffff"+filePath(a))
      if(filePath(a).exists()){
        val out = in(filteInt(a))
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
