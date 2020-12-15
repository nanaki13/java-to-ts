package bon.jo


import bon.jo.ScanJar.{C, OptionTypeScript, ToFileOption, createCimpl}
import scalafx.Includes.{jfxReadOnlyDoubleProperty2sfx, _}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.Pane.sfxPane2jfx

import scala.collection.mutable.ListBuffer







trait JFxDef  extends HaveOneMemo with JfxComponent with JfxEvent  {
  implicit var options: ToFileOption = ToFileOption("")

  var pomFile = ""
  implicit val optionTypeScript: OptionTypeScript = options.optionTypeScript
  def mainContent : BorderPane

  def go(): Unit
}
object JFxDef{
  def apply()(implicit s : PrimaryStage) : JFxDef = new JfxDefImpl
  class JfxDefImpl(implicit s : PrimaryStage) extends JFxDef {
    override lazy val stage: PrimaryStage =s

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


    private val _bp = new BorderPane {
      center = centerP
      bottom = bottomp
      top = menuBar
    }
    def optionContent(implicit e : MExtCmd,in : ExterneCommandes) : Node = {
      val textFieldGitCmdLoc = textFieldGitCmd
      val textFieldMvnCmdLoc = textFieldMvnCmd
      textFieldGitCmdLoc.text = in.git
      textFieldMvnCmdLoc.text = in.mvn
      vb(
        hb(label("git : "), textFieldGitCmdLoc, buttonGitCmd(textFieldGitCmdLoc)),
        hb(label("maven : "), textFieldMvnCmdLoc, buttonMvnCmd(textFieldMvnCmdLoc)))

    }



    def mainContent : BorderPane = _bp
    def go(): Unit = {
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

      val removed = ListBuffer[TreeItem[View]]()

      def filterClassView: Unit = {
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

      filterClassInput.text.onChange(filterClassView)
      bottomp.prefWidthProperty() <== stage.scene.value.widthProperty()
      bottomp.prefHeightProperty() <== stage.scene.value.heightProperty() - centerP.heightProperty() - 10
      classView.prefHeightProperty() <== stage.scene.value.heightProperty() - centerP.heightProperty()
      restoreMemo()

    }
  }

}

