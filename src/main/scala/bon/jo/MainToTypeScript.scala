package bon.jo

import bon.jo.ScanJar.{apply, parseArgs}

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
    case other@_ => parseArgs(other) match {
      case Success( option) => apply()(option)
      case _ =>
    }
  }


}
