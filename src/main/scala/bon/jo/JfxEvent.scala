package bon.jo

import java.io.File
import java.nio.file.Path

import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.SerObject
import javafx.event.Event
import javafx.scene.input.MouseEvent
import scalafx.stage.{DirectoryChooser, FileChooser, Stage}

import scala.sys.process._
import scala.util.{Failure, Success, Try}

trait JfxEvent {
  self: JFx =>


  def stage: Stage


  def log: ProcessLogger = ProcessLogger(appenWithEndLine _)


  protected def appenWithEndLine(string: String) = {
    textArea.appendText(string + "\n")

  }

  object ParseNonRepo {
    def unapply(string: String): Option[String] = {
      Try {
        val start = string.lastIndexOf('/') + 1
        val stop = string.lastIndexOf('.')
        string substring(start, stop)
      } match {
        case Failure(exception) => exception.printStackTrace(); appenWithEndLine(exception.toString); None
        case Success(value) => Some(value)
      }
    }
  }

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

  def jarSouce: File = new File(memo.lastJat.get)

  def jarSouce_=(value: File): Unit = {
    memo = memo.copy(lastJat = Some(value.getAbsolutePath))
    memo.save()
    options = {
      textFieldJar.text = value.getAbsolutePath
      options.copy(value.getAbsolutePath)
    }

  }

  protected def gitCloneTarget: javafx.event.EventHandler[MouseEvent] = event {
    val rrot = new File("app-git-clone")
    rrot.mkdirs()
    val fileGitOption = textFieldRepoGit.getText match {
      case ParseNonRepo(value) => Some(rrot.toPath.resolve(value))
      case _ => None
    }


    fileGitOption.foreach(p => {
      `git cloneOrPull`(textFieldRepoGit.getText, rrot, p)
      `mvn clean package`(p.resolve("pom.xml").toFile)
      p.resolve("target").toFile.listFiles().filter(_.getName.endsWith(".jar")).foreach(jarSouce_=)
    })

  }

  protected def pomTarget[A <: Event] = event[A] {
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

  protected def `mvn clean package`(value: File): Unit = doLater {
    (new java.lang.ProcessBuilder())
      .directory(value.getParentFile).command("mvn.cmd", "package") ! log
  }

  protected def `git cloneOrPull`(repo: String, value: File, repoPath: Path): Unit = doLater {
    if (!repoPath.toFile.exists()) {
      (new java.lang.ProcessBuilder()).directory(value).command("git", "clone", repo) ! log
    }
    (new java.lang.ProcessBuilder()).directory(value).command("git", "pull", repo) ! log
  }

  protected def outTarget: javafx.event.EventHandler[MouseEvent] = { _ =>
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

  protected def launch: javafx.event.EventHandler[MouseEvent] = { _ =>

    Try(ScanJar()) match {
      case Failure(exception) => textArea.text.value = s"${textArea.text.value}\n${exception}:${Option(exception.getMessage).getOrElse("Pas de message")}${Option(exception.getCause).map(_.getMessage).getOrElse("")}"
      case Success(clazzs) => {
        view(clazzs)
        textArea.text.value = s"${textArea.text.value}\nOk"
      }
    }
  }


}
