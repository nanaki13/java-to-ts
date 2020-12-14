package bon.jo

import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object Main extends JFXApp {
  private val jfx = JFxDef()
  stage = new JFXApp.PrimaryStage {
    title.value = "JavaToTs 0.1"
    height = 700
    width = 900
    scene = new Scene {
      fill = Color.AliceBlue
      content = jfx.content
    }
  }
  jfx.go()


}
