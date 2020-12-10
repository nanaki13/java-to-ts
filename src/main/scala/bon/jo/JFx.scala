package bon.jo

import java.io.File
import java.nio.file.Path

import bon.jo.ScanJar.{C, OptionTypeScript, ToFileOption, c, createCimpl}
import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.{IdString, SerObject}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.event.Event
import javafx.scene.input.MouseEvent
import scalafx.Includes.{jfxReadOnlyDoubleProperty2sfx, _}
import scalafx.application.{JFXApp, Platform}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.control.{Button, Label, MultipleSelectionModel, SplitPane, TextArea, TextField, TreeCell, TreeItem, TreeView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.{Node, Scene}
import scalafx.stage.{DirectoryChooser, FileChooser}

import scala.collection.mutable.ListBuffer
import scala.sys.process._
import scala.util.{Failure, Success, Try}

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

  def jarSourceEvent: javafx.event.EventHandler[MouseEvent] = { _ =>
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
          jarSouce = value
      }


      case None =>
    }
  }

  def jarSouce : File = new File(memo.lastJat.get)
  def jarSouce_=(value: File): Unit ={
    memo = memo.copy(lastJat = Some(value.getAbsolutePath))
    memo.save()
    options = {
      textFieldJar.text = value.getAbsolutePath
      options.copy(value.getAbsolutePath)
    }

  }

  object ParseNonRepo {
    def unapply(string: String): Option[String] = {
      Try {
        val start = string.lastIndexOf('/')+1
        val stop = string.lastIndexOf('.')
        string substring(start, stop)
      } match {
        case Failure(exception) => exception.printStackTrace();appenWithEndLine(exception.toString); None
        case Success(value) => Some(value)
      }
    }
  }

  def doLater(f: => Unit) =   Platform.runLater {f}
  def eventDoLater(f: => Unit) : javafx.event.EventHandler[MouseEvent] = { _ =>
    doLater(f)
  }
  def event[A <: Event](f: => Unit) : javafx.event.EventHandler[A] = { _ => f}
  def gitCloneTarget: javafx.event.EventHandler[MouseEvent] = event{
      val rrot = new File("app-git-clone")
    rrot.mkdirs()
    val fileGitOption = textFieldRepoGit.getText match {
      case ParseNonRepo(value) => Some(rrot.toPath.resolve(value))
      case _ => None
    }


      fileGitOption.foreach(p => {
        `git cloneOrPull`(textFieldRepoGit.getText, rrot,p)
        `mvn clean package`(p.resolve("pom.xml").toFile)
        p.resolve("target").toFile.listFiles().filter(_.getName.endsWith(".jar")).foreach(jarSouce_=)
      })

  }

  private def appenWithEndLine(string: String) = {
    textArea.appendText(string + "\n")

  }

  def log: ProcessLogger = ProcessLogger(appenWithEndLine _)

  private def pomTarget[A <: Event] = event[A]{
    val filter = new FileChooser.ExtensionFilter(
      "pom File", "*.xml"
    )
    Option((new FileChooser {
      title = "Choisit un pom"
      extensionFilters.add(filter)


    }).showOpenDialog(stage)) match {
      case Some(value) => {
        pomFile = value.getAbsolutePath
        textFieldPom.text = pomFile
        `mvn clean package`(value)
        value.getParentFile.toPath.resolve("target").toFile.listFiles().filter(_.getName.endsWith(".jar"))
          .foreach(jarSouce_=)
      }
      case None =>
    }
  }

  def `mvn clean package`(value: File): Unit = doLater{
    (new java.lang.ProcessBuilder())
      .directory(value.getParentFile).command("mvn.cmd", "package") ! log
  }

  def `git cloneOrPull`(repo: String, value: File,repoPath : Path): Unit = doLater{
    if(!repoPath.toFile.exists()){
      (new java.lang.ProcessBuilder()).directory(value).command("git", "clone", repo) ! log
    }
    (new java.lang.ProcessBuilder()).directory(value).command("git", "pull", repo) ! log
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

    b
  }
  private val buttonJar = bt("Jar") { b =>
    b.onMouseClicked = jarSourceEvent
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

    b
  }
  private val buttonPom = bt("pom") { b =>
    b.onMouseClicked = pomTarget
    b
  }
  private val buttonGit = bt("clone") { b =>
    b.onMouseClicked = gitCloneTarget
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

    cell
  }

  classView.selectionModel.value.selectedItemProperty().onChange((_,_,new_ ) =>{
    Option(new_).map(_.getValue.c).foreach(cN => {
      textArea.text = cN.toTypeScript
    })
  } )

  private def view(valuep: List[ScanJar.c]): Unit = {
    val ch = valuep.sortWith((a, b) => a.getName < b.getName).map(View).map(_ ())
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
  private val bottomp = sp(vb(filterClassInput, classView), textArea)
  private val marge: Insets = Insets(5)
  private val centerP = vb(


    hb(label("pom : "), textFieldPom, buttonPom),
    hb(label("repo : "), textFieldRepoGit, buttonGit),
    hb(label("jar : "), textFieldJar, buttonJar)
    ,
    hb(label("out : "), textFieldOut, buttonOut)

    , vb(buttonLaunch)
  )
  private val bp = new BorderPane {
    center = centerP
    bottom = bottomp
  }
  stage = new JFXApp.PrimaryStage {
    title.value = "JavaToTs 0.1"
    height = 500
    scene = new Scene {
      fill = Color.AliceBlue
      content = bp
    }

  }


  val removed = ListBuffer[TreeItem[View]]()

  def ch: Unit = {
    classView.getRoot.getChildren.toList.foreach(v => {
      if (!v.getValue.c.getName.contains(filterClassInput.text.getValue)) {
        removed += v
        classView.getRoot.getChildren.remove(v)
      }
    })
    removed.toList.foreach(v => {
      if (v.getValue.c.getName.contains(filterClassInput.text.getValue)) {
        removed -= v
        classView.getRoot.getChildren.add(v)
      }
    })
    classView.getRoot.getChildren.sortInPlaceWith((a, b) => {
      a.getValue.c.getName < b.getValue.c.getName
    })
  }

  filterClassInput.text.onChange(ch)
  stage.sizeToScene()
  bottomp.prefWidthProperty() <== stage.scene.value.widthProperty()
  bottomp.prefHeightProperty() <== stage.scene.value.heightProperty() - centerP.heightProperty() - 10
  restoreMemo()

}
