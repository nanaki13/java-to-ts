package bon.jo.jtots.core

import java.io.FileWriter
import java.nio.file.Paths
import java.util.zip.ZipFile

import ClassToTs._
import ZipOp._
import bon.jo.jtots.config.AllConfig
import bon.jo.jtots.config.AllConfig.TypeScriptConfig

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

object ScanJar extends App {

  def classListByte(jarPath: String): Iterable[(String, Array[Byte])] = (new ZipFile(jarPath)).iterator()

  class CustomCl(class_ : Iterable[(String, Array[Byte])]) extends ClassLoader(Thread.currentThread().getContextClassLoader) {
    val map = class_.map(e => e._1.replace(".class", "").replace("/", ".") -> e._2).toMap

    override def findClass(name: String): Class[_] = {
      val b = map.get(name)
      b match {
        case Some(value) => {
          println(name)
          Try(loadClass(name)) match {
            case Failure(exception) =>   ThrowHandle.noThrow(defineClass(name, value, 0, value.length), null)(e => {
              s"""defineClass $name throw $e"""
            })
            case Success(value) => value
          }

        }
        case None => null
      }

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

  def apply(jarPath: String): Iterable[Class[_]] = new CustomCl(ScanJar.classListByte(jarPath)).classList

  type c = Class[_]

  def apply()(implicit option: AllConfig): List[c] = {
    implicit val optionTypeScript: TypeScriptConfig = option.optionTypeScript
    val out = Paths.get(option.outOption.dirOut).toFile
    val outClass = ListBuffer[c]()
    if (!out.exists()) {
      out.mkdirs()
    }
    ScanJar.apply(option.jar)
      .filter(_.isBeanOrEnum)
      .map(c => {
        outClass += c
        c.toTypeScriptDesc
      })
      .foreach(desc => {
        val f = new FileWriter(Paths.get(option.outOption.dirOut, desc.name).toFile)
        Try(f.write(desc.content))
        f.close()
      })
    outClass.toList
  }

  implicit val option: TypeScriptConfig = TypeScriptConfig()

}
