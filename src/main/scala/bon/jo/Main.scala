package bon.jo

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

  stage.scene = new Scene {
    fill = Color.AliceBlue
    content = jfx.mainContent
  }
  jfx.go()


}
