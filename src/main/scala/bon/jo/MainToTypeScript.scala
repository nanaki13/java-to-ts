package bon.jo

import bon.jo.jtots.config.AllConfig
import bon.jo.jtots.core.ScanJar.apply

import scala.util.Success

object MainToTypeScript extends App {

  Array("--ui") match {
    case Array("--help") | Array() => println(
      """
        |options
        |
        | -j $path : the jar
        | -o $pat : out dir
        | -ts (interface | class) : generate interface or class
        |
        |""".stripMargin)
    case Array("--ui") => Main.main(args)
    case other@_ => AllConfig.parseArgs(other) match {
      case Success( option) => apply()(option)
      case _ =>
    }
  }


}
