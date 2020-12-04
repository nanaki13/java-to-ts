package bon.jo

import bon.jo.ScanJar.ToFileOption
import scalafx.collections.ObservableBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.Stage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.HBox
import scalafx.stage.{DirectoryChooser, FileChooser}
import javafx.scene.input.MouseEvent
import scalafx.event.EventHandler

object JFx extends JFXApp {


  implicit var options: ToFileOption = ToFileOption("")

  val filter = new FileChooser.ExtensionFilter(
    "jar File", "*.jar"
  )

  val jarSource: javafx.event.EventHandler[MouseEvent] = { _ =>
    Option((new FileChooser {
      title = "Choisit un jar"
      extensionFilters.add(filter)

    }).showOpenDialog(stage)) match {
      case Some(value) => options = {
        buttonJar.text = value.getAbsolutePath
        options.copy(value.getAbsolutePath)
      }


      case None =>
    }
  }
  val outTarget: javafx.event.EventHandler[MouseEvent] = { _ =>
    Option((new DirectoryChooser {
      title = "Choisit une sortie"


    }).showDialog(stage)) match {
      case Some(value) => {
        buttonOut.text = value.getAbsolutePath
        options = options.copy(outOption = options.outOption.copy(value.getAbsolutePath))
      }
      case None =>
    }
  }
  val launch : javafx.event.EventHandler[MouseEvent] = { _ => ScanJar()}
  private val buttonJar = new Button {
    text = "Jar"
    onMouseClicked = jarSource
  }
  private val buttonOut = new Button {
    text = "Out"
    onMouseClicked = outTarget
  }
  private val buttonLaunch = new Button {
    text = "Launch"
    onMouseClicked = launch
  }


  stage = new JFXApp.PrimaryStage {
    title.value = "Choisit un jar"
    scene = new Scene {
      content = new HBox {
        children = ObservableBuffer(buttonJar, buttonOut, buttonLaunch

        )
      }
    }
  }
}
