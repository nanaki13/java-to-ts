package bon.jo

import java.io.{BufferedInputStream, ByteArrayInputStream, ByteArrayOutputStream, FileInputStream, FileOutputStream, InputStream, ObjectInputStream, ObjectOutputStream}

import scala.util.{Failure, Success, Try}

object SerPers {

  trait SerPers[A] {
    def save(e: A): Unit

    def restore(e: A): A
  }

  trait SerPersObject[A] {
    def save(): Unit

    def restore(): A
  }

  trait IdString[A] {
    def read(a: A): String
  }

  implicit class SerObject[A: SerPers](a: A) extends SerPersObject[A] {
    type i = SerPers[A]

    override def save(): Unit = implicitly[i].save(a)

    override def restore(): A = implicitly[i].restore(a)
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
        case _ => {
          outp.close()
          fileOut.close()
        }
      }
    }

    override def restore(e: A): A = {
      val out = in(filteInt(e))
      val tr =  Try(out.readObject())
      out.close()
      tr match {
        case Failure(exception) => {
          throw exception
        }
        case Success(value) =>value.asInstanceOf[A]
      }
    }
  }

}
