package bon.jo.jtots.config

import java.nio.file.Paths

import bon.jo.jtots.config
import bon.jo.jtots.config.AllConfig.{AppDir, OutConfig, TypeScriptConfig}

import scala.util.Try

object AllConfig {

  case class TypeScriptConfig(typeScriptClassType: TypeScriptClassType = Interf)

  object TypeScriptConfig {
    def apply(str: String): TypeScriptConfig = {
      TypeScriptConfig(str match {
        case "interface" | "i" => Interf
        case "class" | "c" => _Class
        case _ => Interf
      })
    }
  }

  case class OutConfig(dirOut: String = ".")

  case class AppDir(dirOut: String = Paths.get(System.getProperty("user.home")).resolve(".j-to-ts").toString)

  sealed trait TypeScriptClassType

  case object _Class extends TypeScriptClassType {
    override def toString: String = "class"
  }

  case object Interf extends TypeScriptClassType {
    override def toString: String = "interface"
  }

  def parseArgs(args: Array[String]): Try[AllConfig] = {
    Try(args match {
      case AllConfig(jar, outOption, optionTypeScript) => config.AllConfig(jar, outOption, optionTypeScript)
      case _ => throw new IllegalArgumentException(s"cant parse ${args.toList}")
    })
  }

  def unapply(arg: Array[String]): Option[(String, OutConfig, TypeScriptConfig)] = {
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
    Option((optionMap getOrElse("-j", throw new IllegalArgumentException("option -j non trouvé")),
      OutConfig(optionMap getOrElse("-o", ".")),
      TypeScriptConfig(optionMap getOrElse("-ts", "interface"))))

  }
}

case class AllConfig(jar: String,
                     outOption: OutConfig = OutConfig(),
                     optionTypeScript: TypeScriptConfig = TypeScriptConfig(),appDir: AppDir = AppDir())
