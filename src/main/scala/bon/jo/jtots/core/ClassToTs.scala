package bon.jo.jtots.core

import java.lang.reflect.{Method, Type}
import java.nio.file.Paths

import bon.jo.jtots.config.AllConfig.TypeScriptConfig

object ClassToTs {

  implicit class F(m: Method) {
    def fildName: String = {
      val tm = if (m.getName.startsWith("get"))
        m.getName.replaceFirst("get", "")
      else
        m.getName.replaceFirst("is", "")
      tm.head.toLower + tm.tail
    }

    def nameType: (String, String) = (fildName, m.getReturnType.getGenericInterfaces.headOption.toString)
  }

  case class TypeScriptDesc(name: String, content: String)

  implicit class C(class_ : Class[_])(implicit cop: COp) {
    def getters: Array[Method] = cop.getters(class_)

    def setters: Array[Method] = cop.setters(class_)

    def gettersToFields: Array[(String, Class[_], Type)] = cop.gettersToFields(class_)



    def isBeanOrEnum: Boolean = cop.isBean(class_) || class_.isEnum
    def isBean: Boolean = cop.isBean(class_)
  }
  implicit class CTS(class_ : Class[_])(implicit cop: CWithTs) {
    def imports: Set[String] = cop.imports(class_)

    def toTypeScript: String = cop.toTypeScript(class_)

    def toTypeScriptDesc: TypeScriptDesc = cop.toTypeScriptDesc(class_)
  }

  class FinalClass(class_ : Class[_])(implicit cop: COp) {
    val getters: Array[Method] = cop.getters(class_)

    val setters: Array[Method] = cop.setters(class_)

    val gettersToFields: Array[(String, Class[_], Type)] = cop.gettersToFields(class_)

  }

  trait CWithTs extends COp{
    implicit val option: TypeScriptConfig
    val jsNatifType = Set("string", "number", "boolean", "any", "{}", "Date")

    def nonePrimitif(s: String): Boolean = {
      !jsNatifType.contains(s)
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

    def filePath(class_ : Class[_]) = {
      val f = class_.getName.replace('.','/')
      val fName = f.substring(f.lastIndexOf('/')+1)
      val dir = f.substring(0,f.lastIndexOf('/'))
      val fNameClean = fName.head.toLower+fName.tail.flatMap{ c =>
        if(c.isUpper){
          c.toLower+"-"
        }else{
          s"${c}"
        }

      }
      Paths.get(dir,fNameClean)
    }
    def toTypeScript(class_ : Class[_]): String = {
      if (class_.isEnum) {
        s"""
           |export enum ${class_.getSimpleName}{
           |    ${class_.getEnumConstants.map(en => s"$en='$en'").mkString(",\n    ")}
           |}
           |
           |""".stripMargin
      } else {
        s"""
           |${imports(class_).mkString(";\n")}
           |export ${option.typeScriptClassType} ${class_.getSimpleName}{
           |    ${gettersToFields(class_).toList.map(_.toTypeScript).mkString(";\n    ")};
           |}
           |
           |""".stripMargin
      }

    }

    def toTypeScriptDesc(class_ : Class[_]): TypeScriptDesc = TypeScriptDesc(class_.getSimpleName + ".ts", toTypeScript(class_))
  }
  trait COp {

    def isBean(class_ : Class[_]): Boolean = ThrowHandle.noThrow(gettersToFields(class_).nonEmpty, false)(e => {
      s"""is bean $class_ throw $e"""
    })


    def getters(class_ : Class[_]): Array[Method] = class_.getMethods.filter(m => m.getName != "getClass" && (m.getName.startsWith("get") || m.getName.startsWith("is")))

    def setters(class_ : Class[_]): Array[Method] = class_.getMethods.filter(_.getName.startsWith("set"))

    def gettersToFields(class_ : Class[_]): Array[(String, Class[_], Type)] = getters(class_).filter(e =>
      ThrowHandle.noThrow(class_.getDeclaredFields.map(_.getName).contains(e.fildName), false)
      (e => {
        s"""gettersToFields $class_ throw $e"""
      })

    ).map(g => (g.fildName, class_.getDeclaredField(g.fildName).getType, class_.getDeclaredField(g.fildName).getGenericType))



  }

  implicit class TypeScript(tuple2: (String, Class[_], Type)) {
    def toTypeScript = s""" ${tuple2._1} : ${tuple2.tsType}"""
  }


  case class CImpl()(implicit val option: TypeScriptConfig) extends CWithTs

  implicit def createCimpl(implicit option: TypeScriptConfig): CImpl = CImpl()

  implicit object Cbase extends COp
  implicit class StrToTST(str: (String, Class[_], Type)) {

    def tsType: String = {
      JavaClass.classToProp.get(str._2) match {
        case Some(value) => value.typeScript
        case None => JavaClass.colls.get(str._2) match {
          case Some(value) => value.typeScript(str._3) match {
            case Some(value) => value
            case None => s"erreur parsing col type ${str._2} type ! ${str._3}"
          }
          case _ => {
            s"${str._2.getSimpleName}"
          }
        }
      }
    }
  }

}
