package bon.jo

import java.io.File
import java.nio.file.Paths

import bon.jo.jtots.ui.JFxDef
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object Main extends JFXApp {
  implicit val stage_ : JFXApp.PrimaryStage = new JFXApp.PrimaryStage {
    title.value = "JavaToTs 0.1"
    height = 700
    width = 900

  }
  stage = stage_
  private val jfx = JFxDef()

  val  p = Paths.get(jfx.config.appDir.dirOut)
  p.toFile.mkdirs()
  println(s""" app data in $p""")
  stage.scene = new Scene {
    fill = Color.AliceBlue
    content = jfx.mainContent
  }
  jfx.go()


}
