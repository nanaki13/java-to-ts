package bon.jo

import java.io.File

import bon.jo.ScanJar.ToFileOption
import bon.jo.SerPers.IdString
import bon.jo.SerPers._
import bon.jo.SerPers.ImplicitDef._
import scalafx.collections.ObservableBuffer
import scalafx.application.JFXApp
import scalafx.application.JFXApp.Stage
import scalafx.scene.Scene
import scalafx.scene.control.{Button, TextArea, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.stage.{DirectoryChooser, FileChooser}
import javafx.scene.input.MouseEvent
import scalafx.event.EventHandler

import scala.util.{Failure, Success, Try}

object JFx extends JFXApp {


  case class Memo(lastDir : Option[String]=None,lastJat : Option[String] =None)
  implicit val idMemo : IdString[Memo] = _ => "memo"

  var memo: Memo = {

    Try(Memo().restore()) match {
      case Failure(exception) => Memo()
      case Success(value) => value
    }
  }
  implicit var options: ToFileOption = ToFileOption("")

  val filter = new FileChooser.ExtensionFilter(
    "jar File", "*.jar"
  )

  val jarSource: javafx.event.EventHandler[MouseEvent] = { _ =>
    Option((new FileChooser {
      title = "Choisit un jar"
      extensionFilters.add(filter)
      initialDirectory = memo match {
        case Memo(Some(dir),_) => new File(dir)
        case _ => null
      }

    }).showOpenDialog(stage)) match {
      case v @ Some(value) => {
        memo = memo.copy(lastDir = v.map(_.getAbsolutePath))
        memo.save()
        options = {
          buttonJar.text = value.getAbsolutePath
          options.copy(value.getAbsolutePath)
        }
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
  val launch : javafx.event.EventHandler[MouseEvent] = { _ =>

    Try(ScanJar()) match {
      case Failure(exception) => textArea.text.value  = s"${textArea.text.value}\n${exception}:${Option(exception.getMessage).getOrElse("Pas de message")}${Option(exception.getCause).map(_.getMessage).getOrElse("")}"
      case Success(_) => textArea.text.value = s"${textArea.text.value}\nOk"
    } }
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
  private val textArea = new TextArea


  stage = new JFXApp.PrimaryStage {
    title.value = "Choisit un jar"
    height = 500
    width = 500
    scene = new Scene {
      content = new VBox {
        children = ObservableBuffer(buttonJar, buttonOut, buttonLaunch,textArea
        )
      }
    }
  }
}
