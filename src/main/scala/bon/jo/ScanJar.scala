package bon.jo

import java.io.{FileWriter, InputStream}
import java.lang
import java.lang.reflect.{Method, Type}
import java.nio.file.Paths
import java.util.zip.{ZipEntry, ZipFile}

import bon.jo.ScanJar.OptionTypeScript

import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}

object ScanJar extends App {

  object ZipOp {
    def iterator(zipInputStream: ZipFile): Iterable[(String, Array[Byte])] = {
      new Iterable[(String, Array[Byte])] {
        override def iterator: Iterator[(String, Array[Byte])] = {
          val optionIter = new Iterator[Option[(String, Array[Byte])]] {
            val ent = zipInputStream.entries()

            override def hasNext: Boolean = ent.hasMoreElements

            override def next(): Option[(String, Array[Byte])] = {
              Option[ZipEntry](ent.nextElement()).flatMap(e => {
                if (e.getName.endsWith(".class")) {
                  Option(e)
                } else {
                  None
                }
              }).map((entry: ZipEntry) => {
                (entry.getName, {
                  val zipIn: InputStream = zipInputStream.getInputStream(entry)
                  val buff = ArrayBuffer[Byte]()
                  val ret = new Array[Byte](512)

                  var ecr = zipIn.read(ret, 0, 512)
                  if (ecr != -1) {
                    buff ++= ret.slice(0, ecr)
                  }
                  while (ecr != -1) {
                    ecr = zipIn.read(ret, 0, 512)
                    if (ecr != -1) {
                      buff ++= ret.slice(0, ecr)
                    }
                  }
                  buff.toArray
                })
              })
            }

          }
          optionIter.filter(_.isDefined).map(_.get)
        }
      }
    }
  }

  implicit class ZipOp(zipInputStream: ZipFile) {
    def iterator(): Iterable[(String, Array[Byte])] = {
      ZipOp.iterator(zipInputStream)
    }
  }

  def classListByte(jarPath: String): Iterable[(String, Array[Byte])] = (new ZipFile(jarPath)).iterator()

  class CustomCl(class_ : Iterable[(String, Array[Byte])]) extends ClassLoader(Thread.currentThread().getContextClassLoader) {
    val map = class_.map(e => e._1.replace(".class", "").replace("/", ".") -> e._2).toMap


    override def findClass(name: String): Class[_] = {

      val b = map.get(name)
      b match {
        case Some(value) => {
          println(name)
          noThrow(defineClass(name, value, 0, value.length), null)(e => {
            s"""defineClass $name throw $e"""
          })
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


  case class CImpl()(implicit val option: OptionTypeScript) extends COp

  implicit def createCimpl(implicit option: OptionTypeScript) = CImpl()

  def noThrow[A](a: => A, default: => A)(msg: Throwable => String): A = {
    try {
      a
    } catch {
      case e: Throwable => {
        println(msg(e)); default
      }
    }
  }

  trait COp {
    implicit val option: OptionTypeScript

    def toTypeScriptDesc(class_ : Class[_]): TypeScriptDesc = TypeScriptDesc(class_.getSimpleName + ".ts", toTypeScript(class_))

    def isBean(class_ : Class[_]): Boolean = noThrow(gettersToFields(class_).nonEmpty, false)(e => {
      s"""is bean $class_ throw $e"""
    })


    def getters(class_ : Class[_]): Array[Method] = class_.getMethods.filter(m => m.getName != "getClass" && m.getName.startsWith("get") || m.getName.startsWith("is"))

    def setters(class_ : Class[_]): Array[Method] = class_.getMethods.filter(_.getName.startsWith("set"))

    def gettersToFields(class_ : Class[_]): Array[(String, Class[_], Type)] = getters(class_).filter(e =>
      noThrow(class_.getDeclaredFields.map(_.getName).contains(e.fildName), false)
      (e => {
        s"""gettersToFields $class_ throw $e"""
      })

    ).map(g => (g.fildName, class_.getDeclaredField(g.fildName).getType, class_.getDeclaredField(g.fildName).getGenericType))

    val primitif = Set("string", "number", "boolean", "any")

    def nonePrimitif(s: String): Boolean = {
      !primitif.contains(s)
    }

    def imports(class_ : Class[_]): Set[String] = {


      gettersToFields(class_).filter(a => a._2 != class_
        && !JavaClass.parseType(a._3).contains(class_.getSimpleName))
        .filter(el => !JavaClass.classToProp.contains(el._2)).map(e =>

        JavaClass.colls.get(e._2) match {
          case Some(_) => JavaClass.parseType(e._3).filter(nonePrimitif)
          case _ => Option(s"${e._2.getSimpleName}")
        }
      ).toList.flatMap { e =>
        e.map(v => s"import {${v}} from './${v}'")
      }
    }.toSet

    def toTypeScript(class_ : Class[_]): String = {
      s"""
         |${imports(class_).mkString(";\n")}
         |export ${option.typeScriptClassType} ${class_.getSimpleName}{
         |    ${gettersToFields(class_).toList.map(_.toTypeScript).mkString(";\n    ")};
         |}
         |
         |""".stripMargin
    }

  }

  case class TypeScriptDesc(name: String, content: String)

  implicit class C(class_ : Class[_])(implicit cop: COp) {
    def getters: Array[Method] = cop.getters(class_)

    def setters: Array[Method] = cop.setters(class_)

    def gettersToFields: Array[(String, Class[_], Type)] = cop.gettersToFields(class_)

    def imports: Set[String] = cop.imports(class_)

    def toTypeScript: String = cop.toTypeScript(class_)

    def toTypeScriptDesc: TypeScriptDesc = cop.toTypeScriptDesc(class_)

    def isBean: Boolean = cop.isBean(class_)
  }

  class FinalClass(class_ : Class[_])(implicit cop: COp) {
    val getters: Array[Method] = cop.getters(class_)

    val setters: Array[Method] = cop.setters(class_)

    val gettersToFields: Array[(String, Class[_], Type)] = cop.gettersToFields(class_)

  }

  implicit class F(m: Method) {
    def fildName: String = {
      val tm = m.getName.replaceFirst("get", "")
      tm.head.toLower + tm.tail
    }

    def nameType: (String, String) = (fildName, m.getReturnType.getGenericInterfaces.headOption.toString)
  }

  object JavaClass {

    import java.{lang => jl, util => ju}

    def string: Class[jl.String] = classOf
    def long: Class[jl.Long] = classOf
    def int: Class[jl.Integer] = classOf
    def short: Class[jl.Short] = classOf
    def byte: Class[jl.Byte] = classOf
    def boolean: Class[jl.Boolean] = classOf
    def date: Class[ju.Date] = classOf
    def float: Class[jl.Float] = classOf
    def double: Class[jl.Double] = classOf
    def list: Class[ju.List[_]] = classOf
    def arrayList: Class[ju.ArrayList[_]] = classOf
    def linkedList: Class[ju.LinkedList[_]] = classOf
    def intP: Class[Integer] = jl.Integer.TYPE
    def longP: Class[lang.Long] = jl.Long.TYPE
    def floatP: Class[lang.Float] = jl.Float.TYPE
    def doubleP: Class[lang.Double] = jl.Double.TYPE
    def booleanP: Class[lang.Boolean] = jl.Boolean.TYPE
    def byteP: Class[lang.Byte] = jl.Byte.TYPE
    def shortP: Class[lang.Short] = jl.Short.TYPE

    case class Prop(typeScript: String)

    case class PropColl(typeScript: Type => Option[String])

    private def p = Prop(_)

    private val genParse = """.*<(.*)>""".r
    private val pNumber = p("number")
    private val pString = p("string")
    private val pBoolean = p("boolean")
    private val pDate = p("Date")
    val classToProp: Map[Class[_], Prop] = Map(
      string -> pString,
      boolean -> pBoolean,
      date -> pDate,
      float -> pNumber,
      double -> pNumber,
      long -> pNumber,
      int -> pNumber,
      short -> pNumber,
      byte -> pNumber,
      intP -> pNumber,
      longP -> pNumber,
      floatP -> pNumber,
      doubleP -> pNumber,
      booleanP -> pBoolean,
      byteP -> pBoolean,
      shortP -> pBoolean
    )
    val stringToProp: Map[String, Prop] = Map(
      "long" -> pNumber,
      "int" -> pNumber,
      "integer" -> pNumber,
      "float" -> pNumber,
      "double" -> pNumber,
      "boolean" -> pBoolean,
      "string" -> pString,
      "Long" -> pNumber,
      "Integer" -> pNumber,
      "Float" -> pNumber,
      "Double" -> pNumber,
      "Boolean" -> pBoolean,
      "String" -> pString,
      "date" -> pDate,
      "LocalDate" -> pDate,
      "LocalDateTime" -> pDate,
      "LocalTime" -> pDate,
    )

    private def pt = PropColl(_)

    private def parseColType(type_ : Type): Option[String] = {
      type_.getTypeName match {
        case genParse(colElmType) => Option(s"${checkIfLgType(colElmType)}[]")
        case _ => None
      }
    }

    def checkIfLgType(colElmType: String): String = {
      val simpleName = colElmType.split("\\.").last.split("\\$").last
      stringToProp.getOrElse(simpleName, p(simpleName)).typeScript
    }

    def parseType(type_ : Type): Option[String] = {
      type_.getTypeName match {
        case genParse(colElmType) => Option(checkIfLgType(colElmType))
        case _ => None
      }
    }

    val propColPr = pt(parseColType)
    val colls: Map[Class[_], PropColl] = Map(
      list -> propColPr,
      arrayList -> propColPr,
      linkedList -> propColPr,
    )

  }


  implicit class StrToTST(str: (String, Class[_], Type)) {

    def tsType: String = {
      JavaClass.classToProp.get(str._2) match {
        case Some(value) => value.typeScript
        case None => JavaClass.colls.get(str._2) match {
          case Some(value) => value.typeScript(str._3) match {
            case Some(value) => value
            case None => s"erreur parsing col type ${str._2} type ! ${str._3}"
          }
          case _ => s"${str._2.getSimpleName}"
        }
      }
    }
  }

  implicit class TypeScript(tuple2: (String, Class[_], Type)) {
    def toTypeScript = s""" ${tuple2._1} : ${tuple2.tsType}"""
  }


  def apply()(implicit option: ToFileOption): Unit = {
    implicit val optionTypeScript: OptionTypeScript = option.optionTypeScript
    val out = Paths.get(option.outOption.dirOut).toFile
    if (!out.exists()) {
      out.mkdirs()
    }
    ScanJar.apply(option.jar)
      .filter(_.isBean)
      .map(c => (c.toTypeScriptDesc))
      .foreach(desc => {
        val f = new FileWriter(Paths.get(option.outOption.dirOut, desc.name).toFile)
        Try(f.write(desc.content))
        f.close()
      })
  }

  case class OptionTypeScript(typeScriptClassType: TypeScriptClassType = Interf)

  object OptionTypeScript {
    def apply(str: String): OptionTypeScript = {
      OptionTypeScript(str match {
        case "interface" | "i" => Interf
        case "class" | "c" => _Class
        case _ => Interf
      })
    }
  }

  case class ToFileOption(jar: String, outOption: OutOption = OutOption(), optionTypeScript: OptionTypeScript = OptionTypeScript())

  object ToFileOption {
    def unapply(arg: Array[String]): Option[(String, OutOption, OptionTypeScript)] = {
      var optionName: List[String] = List()
      var optionValue: List[String] = List()
      arg.foreach(e => {
        if (e.startsWith("-")) {
          optionName = optionName :+ e
        } else {
          optionValue = optionValue :+ e
        }
      })
      val optionMap = (optionName zip optionValue).toMap
      Option((optionMap getOrElse("-j", throw new IllegalArgumentException("option -j non trouvé")), OutOption(optionMap getOrElse("-o", ".")), OptionTypeScript(optionMap getOrElse("-ts", "interface"))))

    }
  }

  case class OutOption(dirOut: String = ".")

  sealed trait TypeScriptClassType

  case object _Class extends TypeScriptClassType {
    override def toString: String = "class"
  }

  case object Interf extends TypeScriptClassType {
    override def toString: String = "interface"
  }

  def parseArgs(args: Array[String]) = {
    Try(args match {
      case ToFileOption(jar, outOption, optionTypeScript) => ToFileOption(jar, outOption, optionTypeScript)
      case _ => throw new IllegalArgumentException(s"cant parse ${args.toList}")
    })
  }

  implicit val option: OptionTypeScript = OptionTypeScript()

  println(classOf[UserJava].toTypeScript)

}
