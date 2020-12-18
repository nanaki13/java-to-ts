package bon.jo.jtots.core

import java.util.zip.ZipFile

import bon.jo.jtots.config.AllConfig.TypeScriptConfig
import bon.jo.jtots.core.ClassToTs.{Cbase,C}
import bon.jo.jtots.core.ZipOp._

object ScanJar {

  def classListByte(jarPath: String): Iterable[(String, Array[Byte])] = (new ZipFile(jarPath)).iterator()


  class CustomCl(class_ : Iterable[(String, Array[Byte])]) extends ClassLoader(Thread.currentThread().getContextClassLoader) {
    val map = class_.map(e => e._1.replace(".class", "").replace("/", ".") -> e._2).toMap

    val mu = scala.collection.mutable.Map[String, Class[_]]()

    override def findClass(name: String): Class[_] = {
      val b = map.get(name)
      mu.getOrElseUpdate(name, b match {
        case Some(value) => {
          ThrowHandle.noThrow(defineClass(name, value, 0, value.length), null)(e => {
            s"""defineClass $name throw $e"""
          })
        }
        case None => null
      })
    }

    def findClassOption(name: String): Option[Class[_]] = {
      Option(try {
        (findClass(name))
      } catch {
        case e: Throwable => {
          e.printStackTrace()
          null
        }
      })

    }

    def classList: Iterable[Class[_]] = map.keys.flatMap(findClassOption)
  }

  def listClass(jarPath: String): Iterable[Class[_]] = new CustomCl(ScanJar.classListByte(jarPath)).classList


  def listBeanOrEnumClass(jar : String)(implicit option: TypeScriptConfig): Iterable[ProcessClass.c] = {
    listClass(jar)
      .filter(_.isBeanOrEnum)
  }

}
