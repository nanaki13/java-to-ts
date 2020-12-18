package bon.jo.jtots.core

import java.io.FileWriter
import java.nio.file.Paths

import bon.jo.jtots.config.AllConfig.{OutConfig, TypeScriptConfig}

import bon.jo.jtots.core.ClassToTs.{CTS,CWithTs,createCimpl,C}
import scala.util.Try

object ProcessClass {

  type c = Class[_]
  def toTsDirectory(l: List[c])(implicit outConfig: OutConfig, typeScriptConfig: TypeScriptConfig): Unit = {

    //implicit val processC: CWithTs = createCimpl
    val out = Paths.get(outConfig.dirOut).toFile
    if (!out.exists()) {
      out.mkdirs()
    }
    l.filter(_.isBeanOrEnum).foreach(desc => {
      val descL = desc.toTypeScriptDesc
      val f = new FileWriter(Paths.get(outConfig.dirOut, descL.name).toFile)
      Try(f.write(descL.content))
      f.close()
    })
  }


}
