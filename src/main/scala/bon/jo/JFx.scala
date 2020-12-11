package bon.jo

import bon.jo.ScanJar.{C, OptionTypeScript, ToFileOption, createCimpl}
import scalafx.Includes.{jfxReadOnlyDoubleProperty2sfx, _}
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane
import scalafx.scene.paint.Color
import scalafx.stage.Stage

import scala.collection.mutable.ListBuffer







trait JFx  extends HaveOneMemo with JfxComponent with JfxEvent  {
  implicit var options: ToFileOption = ToFileOption("")
  var pomFile = ""
  implicit val optionTypeScript: OptionTypeScript = options.optionTypeScript
}
object JFx extends JFXApp   {

  object JfxImpl extends JFx {
    override def stage: Stage = JFx.stage
  }

  import JfxImpl._
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

  classView.selectionModel.value.selectedItemProperty().onChange((_, _, new_) => {
    Option(new_).map(_.getValue.c).flatMap(Option(_)).foreach(cN => {
      textArea.text = cN.toTypeScript
    })
  })



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



  private val filterClassInput = new TextField
  private val bottomp = sp(vb(filterClassInput, classView), textArea)

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
