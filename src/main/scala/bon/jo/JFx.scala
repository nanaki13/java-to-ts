package bon.jo

import java.io.File

import bon.jo.ScanJar.{C, OptionTypeScript, ToFileOption, c, createCimpl}
import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.{IdString, SerObject}
import javafx.scene.input.MouseEvent
import scalafx.application.JFXApp
import scalafx.collections.ObservableBuffer
import scalafx.event.EventType
import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Button, Label, ScrollPane, SplitPane, TextArea, TextField, TreeCell, TreeItem, TreeView}
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.stage.{DirectoryChooser, FileChooser}

import scala.util.{Failure, Success, Try}

object JFx extends JFXApp {


  case class Memo(lastDir: Option[String] = None, lastJat: Option[String] = None)

  implicit val idMemo: IdString[Memo] = _ => "memo"

  var memo: Memo = {
    Try(Memo().restore()) match {
      case Failure(exception) => Memo()
      case Success(value) => value
    }
  }
  implicit var options: ToFileOption = ToFileOption("")
  implicit val optionTypeScript: OptionTypeScript = options.optionTypeScript

  def jarSource: javafx.event.EventHandler[MouseEvent] = { _ =>
    val filter = new FileChooser.ExtensionFilter(
      "jar File", "*.jar"
    )
    Option((new FileChooser {
      title = "Choisit un jar"
      extensionFilters.add(filter)
      initialDirectory = memo match {
        case Memo(_, Some(jar)) => (new File(jar)).getParentFile
        case _ => null
      }

    }).showOpenDialog(stage)) match {
      case v@Some(value) => {
        memo = memo.copy(lastJat = v.map(_.getAbsolutePath))
        memo.save()
        options = {
          textFieldJar.text = value.getAbsolutePath
          options.copy(value.getAbsolutePath)
        }
      }


      case None =>
    }
  }

  def outTarget: javafx.event.EventHandler[MouseEvent] = { _ =>
    Option((new DirectoryChooser {
      title = "Choisit une sortie"
      initialDirectory = memo match {
        case Memo(Some(dir), _) => new File(dir)
        case _ => null
      }

    }).showDialog(stage)) match {
      case Some(value) => {
        memo = memo.copy(lastDir = Some(value.getAbsolutePath))
        memo.save()
        textFieldOut.text = value.getAbsolutePath
        options = options.copy(outOption = options.outOption.copy(value.getAbsolutePath))
      }
      case None =>
    }
  }

  def launch: javafx.event.EventHandler[MouseEvent] = { _ =>

    Try(ScanJar()) match {
      case Failure(exception) => textArea.text.value = s"${textArea.text.value}\n${exception}:${Option(exception.getMessage).getOrElse("Pas de message")}${Option(exception.getCause).map(_.getMessage).getOrElse("")}"
      case Success(clazzs) => {
        view(clazzs)
        textArea.text.value = s"${textArea.text.value}\nOk"
      }
    }
  }

  def bt(textS: String)(c: Button => Button): Button = c(new Button {
    text = textS
    margin = marge
  })

  def tf(textS: String)(c: TextField => TextField): TextField = c(new TextField {
    text = textS
    margin = marge
  })

  private val textFieldJar = tf("") { b =>
    b.editable = false
    b
  }
  private val buttonJar = bt("Jar") { b =>
    b.onMouseClicked = jarSource
    b
  }
  private val textFieldOut = tf("") { b =>
    b.editable = false
    b
  }
  private val buttonOut = bt("Out") { b =>
    b.onMouseClicked = outTarget
    b
  }
  private val buttonLaunch = bt("Launch") { b =>
    b.onMouseClicked = launch
    b
  }

  def resize: javafx.event.EventHandler[MouseEvent] = { _ =>

  }

  private val buttonResize = bt("Resize") { b =>
    b.onMouseClicked = resize
    b
  }
  private val textArea = new TextArea {
    margin = Insets(5)
  }

  case class View(c: c) {
    def apply(): TreeItem[View] = new TreeItem[View](this)

    override def toString: String = c.getName
  }

  object rootV extends View(null) {
    override def toString: String = "+"
  }

  val rNode = new TreeItem[View] {
    value = rootV
  }
  private val classView = new TreeView[View] {
    root = rNode
    rNode.setExpanded(true)
  }
  classView.cellFactory = v => {
    val cell = new TreeCell[View](new javafx.scene.control.TreeCell[View] {
      override def updateItem(t: View, empty: Boolean): Unit = {
        super.updateItem(t, empty)
        if (empty) {
          setText(null)
          setGraphic(null)
        }
        else {
          {
            setText(t.toString)
            setGraphic(getTreeItem.getGraphic)
          }
        }


      }
    })
    cell.onMouseClicked = _ => {
      if (!cell.isEmpty) {
        val v: View = cell.treeItem.value.getValue
        Option(v.c).foreach(cN => {
          println(cN)
          textArea.text = cN.toTypeScript
        })
      }
    }
    cell
  }
  classView.onEditStart = _ => println("Yo")

  private def view(valuep: List[ScanJar.c]): Unit = {
    val ch = valuep.map(View).map(_ ())
    rNode.children = ch
    rNode.setExpanded(true)
  }

  private def changeDirOut(path: String) = {
    options = options.copy(outOption = options.outOption.copy(path))
  }

  private def changeJar(jar: String): Unit = options = options.copy(jar = jar)

  private def restoreMemo(): Unit = {

    memo.lastDir.foreach(lDir => {
      changeDirOut(lDir)
      textFieldOut.text = lDir
      textFieldOut.setPrefWidth(lDir.length * 7)

    })
    memo.lastJat.foreach(jar => {
      changeJar(jar)
      textFieldJar.text = jar
      textFieldJar.setPrefWidth(jar.length * 7)
    })

  }

  def label(txt: String) = new Label {
    text = txt;
    margin = marge
  }

  def hb(chhi: Node*) = {
    new HBox {
      children = ObservableBuffer(chhi)
      margin = marge
    }
  }

  def vb(chhi: Node*) = {
    new VBox() {
      children = ObservableBuffer(chhi)
      margin = marge
    }
  }
  def sp(textArea: Node, classView: Node): Node = {
    val ct = new SplitPane {
      orientation = Orientation.Horizontal
      items.addAll(textArea, classView)
    }


    ct
  }

  private val marge: Insets = Insets(5)
  stage = new JFXApp.PrimaryStage {
    title.value = "Choisit un jar"
    scene = new Scene {


      content =   new ScrollPane{vb(
          buttonResize,
          hb(label("jar : "), textFieldJar, buttonJar)
          ,
          hb(label("out : "), textFieldOut, buttonOut)
          , vb(buttonLaunch, sp(textArea, classView))
        )}
       

    }

  }

  restoreMemo()
  stage.sizeToScene()
}
