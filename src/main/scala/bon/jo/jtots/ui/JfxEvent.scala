package bon.jo.jtots.ui

import java.io.File
import java.nio.file.{Path, Paths}

import bon.jo.Memo
import bon.jo.jtots.config.AllConfig
import bon.jo.jtots.config.AllConfig.{AppDir, OutConfig, TypeScriptConfig}
import bon.jo.jtots.core.{ProcessClass, ScanJar}
import javafx.event.{Event, EventHandler}
import javafx.scene.input.MouseEvent
import scalafx.scene.control.{Button, TextField}
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process.ProcessLogger
import scala.sys.process._
import scala.util.{Failure, Success, Try}
import bon.jo.jtots.core.SerPers._

trait JfxEvent {
  self: JFxDef =>

  import hadleMemo._

  val stage: Stage


  def log: ProcessLogger = ( ProcessLogger(appendWithEndLine _))


  protected def appendWithEndLine(string: String): Unit = {
    uiDoLater( textArea.appendText(string + "\n"))

  }

  object ParseNonRepo {
    def unapply(string: String): Option[String] = {
      Try {
        val start = string.lastIndexOf('/') + 1
        val stop = string.lastIndexOf('.')
        string substring(start, stop)
      } match {
        case Failure(exception) => exception.printStackTrace(); appendWithEndLine(exception.toString); None
        case Success(value) => Some(value)
      }
    }
  }

  def jarSourceEvent: javafx.event.EventHandler[MouseEvent] = { _ =>
    val filter = new FileChooser.ExtensionFilter(
      "jar File", "*.jar"
    )
    Option(new FileChooser {
      title = "Choisit un jar"
      extensionFilters.add(filter)
      initialDirectory = hadleMemo.memo match {
        case Memo(_, Some(jar), _) => new File(jar).getParentFile
        case _ => null
      }

    }.showOpenDialog(stage)) match {
      case Some(value) =>
        jarSource = value


      case None =>
    }
  }

  def jarSource: File = new File(hadleMemo.memo.lastJat.get)

  def jarSource_=(value: File): Unit = {
    memo = memo.copy(lastJat = Some(value.getAbsolutePath))



    memo.save()
    options = {
      textFieldJar.textProperty().setValue(value.getAbsolutePath)
      options.copy(value.getAbsolutePath)
    }

  }

  protected def gitCmdChoose(textField: TextField)(implicit e: MExtCmd, in: ExterneCommandes): javafx.event.EventHandler[MouseEvent] = event {

    Option(new FileChooser {
      title = "Choisit un exe ou script"
      Option(Paths.get(in.git).getParent) map (_.toFile) foreach initialDirectory.set
    }.showOpenDialog(stage)) match {
      case Some(value) =>
        e.value = e.value.copy(git = value.getAbsolutePath)
        textField.text = value.getAbsolutePath
      case None =>
    }

  }

  case class Mutable[A](var value: A)

  type MExtCmd = Mutable[ExterneCommandes]

  protected def mvnCmdChoose(textField: TextField)(implicit e: MExtCmd, in: ExterneCommandes): javafx.event.EventHandler[MouseEvent] = event {
    {
      textField.text = in.mvn
      Option(new FileChooser {
        title = "Choisit un exe ou script"
        Option(Paths.get(in.mvn).getParent) map (_.toFile) foreach initialDirectory.set
      }.showOpenDialog(stage)) match {
        case Some(value) =>
          e.value = e.value.copy(mvn = value.getAbsolutePath)
          textField.text = value.getAbsolutePath
        case None =>
      }

    }
  }

  private def outGit = Paths.get(appDir.dirOut).resolve("app-git-clone").toFile
  protected def gitCloneTarget: javafx.event.EventHandler[MouseEvent] = event {

    val repo = textFieldRepoGit.getText
    Future {
      val old = runningStart(buttonGit)
      val rrot = outGit
      rrot.mkdirs()
      val fileGitOption = repo match {
        case ParseNonRepo(value) => Some(rrot.toPath.resolve(value))
        case _ => None
      }
      fileGitOption.foreach(p => {


        `git cloneOrPull`(repo, rrot, p)
        `mvn clean package`(p.resolve("pom.xml").toFile)
        println(p.resolve("target").toFile.listFiles().toList)
        uiDoLater {
          p.resolve("target").toFile.listFiles().toList
            .filter(_.getName.endsWith(".jar")).foreach(jarSource_=)
          buttonGit.textProperty().setValue("clone")
        }
      })
      runningStop(buttonGit, old)
    }
  }

  protected def pomTarget[A <: Event]: EventHandler[A] = event[A] {


    val filter = new FileChooser.ExtensionFilter(
      "pom File", "*.xml"
    )
    Option(new FileChooser {
      title = "Choisit un pom"
      extensionFilters.add(filter)


    }.showOpenDialog(stage)) match {
      case Some(value) =>

        pomFile = value.getAbsolutePath
        textFieldPom.text = pomFile
        Future {
          val old = runningStart(buttonPom)
          `mvn clean package`(value)
        uiDoLater( value.getParentFile.toPath.resolve("target").toFile.listFiles().filter(_.getName.endsWith(".jar"))
            .foreach(jarSource_=))
          runningStop(buttonPom, old)
    }
      case None =>
    }


  }

  protected def `mvn clean package`(value: File): Unit = {
    new java.lang.ProcessBuilder()
      .directory(value.getParentFile).command(memo.externeCommandes.mvn, "package") ! log
  }

  protected def `git cloneOrPull`(repo: String, value: File, repoPath: Path): Unit = {
    println(repo: String, value: File, repoPath: Path)
    if (!repoPath.toFile.exists() || repoPath.toFile.listFiles().isEmpty) {
      new java.lang.ProcessBuilder().directory(value).command(memo.externeCommandes.git, "clone", repo) ! log
    }else{
      new java.lang.ProcessBuilder().directory(repoPath.toFile).command(memo.externeCommandes.git, "pull", repo) ! log
    }
  }

  protected def outTarget: javafx.event.EventHandler[MouseEvent] = { _ =>
    Option(new DirectoryChooser {
      title = "Choisit une sortie"
      initialDirectory = memo match {
        case Memo(Some(dir), _, _) => new File(dir)
        case _ => null
      }

    }.showDialog(stage)) match {
      case Some(value) =>
        memo = memo.copy(lastDir = Some(value.getAbsolutePath))
        memo.save()
        textFieldOut.text = value.getAbsolutePath
        options = options.copy(outOption = options.outOption.copy(value.getAbsolutePath))
      case None =>
    }
  }

  def runningStart(b: Button): String = {
    val old = b.text.value
    uiDoLater {
      b.disable = true
      b.text.setValue("Running")
    }
    old
  }

  def runningStop(b: Button, old: String): Unit = {
    uiDoLater {
      b.disable = false
      uiDoLater(b.text.setValue(old))
    }
  }

  protected def viewTree: javafx.event.EventHandler[MouseEvent] = { _ =>
    Future {
      val cf = Cf()
      import cf._
      val old = runningStart(buttonViewTree)
      Try(ScanJar.listBeanOrEnumClass(options.jar)) match {
        case Failure(exception) => uiDoLater(textArea.text.value = s"${textArea.text.value}\n$exception:${Option(exception.getMessage).getOrElse("Pas de message")}${Option(exception.getCause).map(_.getMessage).getOrElse("")}")
        case Success(clazzs) => uiDoLater {
          view(clazzs.toList)
          textArea.text.value = s"${textArea.text.value}\nOk"
        }
      }
      uiDoLater(runningStop(buttonViewTree, old))
    }
  }
  case class Cf(){
    val allConfig : AllConfig = implicitly
    implicit val outConfig: OutConfig = allConfig.outOption
    implicit val typeScriptConfig: TypeScriptConfig = allConfig.optionTypeScript

  }
  def conf(f : =>Unit) = {

  }
  protected def typeScriptToOut :  javafx.event.EventHandler[MouseEvent] = { _ =>
    Future {
       val cf = Cf()
       import cf._
        val old = runningStart(buttonTsToOutDir)
        Try(ScanJar.listBeanOrEnumClass(options.jar)) match {
          case Failure(exception) => uiDoLater(textArea.text.value = s"${textArea.text.value}\n$exception:${Option(exception.getMessage).getOrElse("Pas de message")}${Option(exception.getCause).map(_.getMessage).getOrElse("")}")
          case Success(clazzs) => uiDoLater {
            ProcessClass.toTsDirectory(clazzs.toList)
            textArea.text.value = s"${textArea.text.value}\nOk"
          }
        }
        uiDoLater(runningStop(buttonTsToOutDir, old))


    }
  }

}
