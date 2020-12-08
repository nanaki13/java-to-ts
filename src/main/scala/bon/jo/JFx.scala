package bon.jo

import java.io.File

import scalafx.Includes._
import bon.jo.ScanJar.{C, OptionTypeScript, ToFileOption, c, createCimpl}
import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.{IdString, SerObject}
import javafx.beans.value.ObservableValue
import javafx.scene.input.MouseEvent
import scalafx.Includes.jfxReadOnlyDoubleProperty2sfx
import scalafx.application.JFXApp
import scalafx.beans.property.{Property, StringProperty}
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.event.EventType
import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.{Button, Label, ScrollPane, SplitPane, TextArea, TextField, TreeCell, TreeItem, TreeView}
import scalafx.scene.layout.{BorderPane, HBox, Pane, VBox}
import scalafx.scene.paint.Color
import scalafx.stage.{DirectoryChooser, FileChooser}

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}
import scala.sys.process._
object JFx extends JFXApp {


  case class Memo(lastDir: Option[String] = None
                  , lastJat: Option[String] = None

                 )

  implicit val idMemo: IdString[Memo] = _ => "memo"

  var memo: Memo = {
    Try(Memo().restore()) match {
      case Failure(exception) => Memo()
      case Success(value) => value
    }
  }
  implicit var options: ToFileOption = ToFileOption("")
  var pomFile = ""
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



  def gitCloneTarget : javafx.event.EventHandler[MouseEvent] = { _ =>
      textFieldRepoGit.getText

    `git clone`(textArea.getText,new File("app-git-clone"))
  }

  def log: ProcessLogger = ProcessLogger(textArea.appendText _ )

  def pomTarget: javafx.event.EventHandler[MouseEvent] = { _ =>
    val filter = new FileChooser.ExtensionFilter(
      "pom File", "*.xml"
    )
    Option((new FileChooser {
      title = "Choisit un pom"
      extensionFilters.add(filter)
//      initialDirectory = memo match {
//        case Memo(_,_, Some(pom)) => (new File(pom)).getParentFile
//        case _ => null
//      }

    }).showOpenDialog(stage)) match {
      case Some(value) => {
        pomFile = value.getAbsolutePath
        textFieldPom.text = pomFile


        `mvn clean package` (value)

//        memo = memo.copy(lastJat = v.map(_.getAbsolutePath))
//        memo.save()
//        options = {
//          textFieldJar.text = value.getAbsolutePath
//          options.copy(value.getAbsolutePath)
//        }

      }


      case None =>
    }
  }

  def `mvn clean package`(value : File): Int =  (new java.lang.ProcessBuilder())
    .directory(value.getParentFile).command( "mvn.cmd","clean","package") ! log
  def `git clone`(repo : String, value : File): Int = {
    value.mkdirs()
    (new java.lang.ProcessBuilder()).directory(value).command("git", "clone", repo) ! log
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
  private val textFieldRepoGit = tf("") { b =>
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
  private val textFieldPom = tf("") { b =>
    b.editable = false
    b
  }
  private val buttonPom = bt("pom") { b =>
    b.onMouseClicked = pomTarget
    b
  }
  private val buttonGit= bt("clone") { b =>
    b.onMouseClicked = pomTarget
    b
  }
  private val buttonLaunch = bt("Launch") { b =>
    b.onMouseClicked = launch
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
    val ch = valuep.sortWith((a,b)=>a.getName < b.getName).map(View).map(_ ())
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
  def sp(textArea: Node, classView: Node): SplitPane = {
    val ct = new SplitPane {
      orientation = Orientation.Horizontal
      items.addAll(textArea, classView)
    }


    ct
  }
  private val filterClassInput = new TextField
  private val bottomp = sp(vb(filterClassInput, classView),textArea)
  private val marge: Insets = Insets(5)
  private val centerP =  vb(


    hb(label("pom : "), textFieldPom, buttonPom),
    hb(label("repo : "), textFieldRepoGit, buttonGit),
    hb(label("jar : "), textFieldJar, buttonJar)
    ,
    hb(label("out : "), textFieldOut, buttonOut)

    , vb(buttonLaunch)
  )
  private val bp  = new BorderPane{
    center = centerP
    bottom = bottomp
  }
  stage = new JFXApp.PrimaryStage {
    title.value = "Choisit un jar"
    height = 500
    scene = new Scene {
      fill =  Color.AliceBlue
      content =  bp
    }

  }


  val removed = ListBuffer[TreeItem[View]]()

  def ch :Unit= {
    classView.getRoot.getChildren.toList.foreach(v => {
      if(!v.getValue.c.getName.contains(filterClassInput.text.getValue)){
        removed += v
        classView.getRoot.getChildren.remove(v)
      }
    })
    removed.toList.foreach(v=>{
      if(v.getValue.c.getName.contains(filterClassInput.text.getValue)){
        removed -= v
        classView.getRoot.getChildren.add(v)
      }
    })
    classView.getRoot.getChildren.sortInPlaceWith((a,b)=>{
      a.getValue.c.getName < b.getValue.c.getName
    })
  }

  filterClassInput.text.onChange(ch)
  stage.sizeToScene()
  bottomp.prefWidthProperty() <== stage.scene.value.widthProperty()
  bottomp.prefHeightProperty() <== stage.scene.value.heightProperty() - centerP.heightProperty() - 10
  restoreMemo()

}
