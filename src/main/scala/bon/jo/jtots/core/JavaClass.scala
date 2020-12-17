package bon.jo.jtots.core

import java.lang
import java.lang.reflect.Type

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

  def map: Class[ju.Map[_, _]] = classOf

  def list: Class[ju.List[_]] = classOf

  def arrayList: Class[ju.ArrayList[_]] = classOf

  def set: Class[ju.Set[_]] = classOf

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
  private val pObject = p("{}")
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
    shortP -> pBoolean,
    map -> pObject
  )
  val stringToProp: Map[String, Prop] = Map(
    "long" -> pNumber,
    "int" -> pNumber,
    "integer" -> pNumber,
    "short" -> pNumber,
    "Short" -> pNumber,
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
    set -> propColPr,
    linkedList -> propColPr,
  )

}
