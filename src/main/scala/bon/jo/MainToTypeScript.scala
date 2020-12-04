package bon.jo

import bon.jo.ScanJar.{classToTypeScriptFile, parseArgs}

import scala.util.Success

object MainToTypeScript extends App {

  args match {
    case Array("--help") | Array() => println(
      """
        |options
        |
        | -j $path : the jar
        | -o $pat : out dir
        | -ts (interface | class) : generate interface or class
        |
        |""".stripMargin)
    case other@_ => parseArgs(other) match {
      case Success(option) => classToTypeScriptFile(option)
      case _ =>
    }
  }


}
