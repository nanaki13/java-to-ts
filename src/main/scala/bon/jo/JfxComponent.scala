package bon.jo

import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.SerObject
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.paint.Color
import scalafx.scene.{Node, Scene}
import scalafx.stage.Modality

trait JfxComponent extends JfXHelper {
  self: JfxEvent with JFxDef =>


  def mainContent: Node

  def optionContent(implicit v : MExtCmd,in : ExterneCommandes): Node

  object rootV extends View(null) {
    override def toString: String = "+"
  }

  protected val rNode: TreeItem[View] = new TreeItem[View] {
    value = rootV
  }
  protected val classView: TreeView[View] = new TreeView[View] {
    root = rNode
    rNode.setExpanded(true)
  }

  def emptyTf(editable: Boolean = true): TextField = tf("") { b =>
    b.editable = editable
    b
  }

  protected val menuBar: MenuBar = new MenuBar {
    menus = ObservableBuffer(new Menu {
      text = "option"
      items = List(new MenuItem {
        text = "commande externes"
        onAction = _ => {
          val cmdnew = Mutable(memo.externeCommandes)
          val p: Dialog[Boolean] = new Dialog[Boolean]() {
            initModality(Modality.WindowModal)
            initOwner(stage)

            dialogPane.value.setContent(optionContent(cmdnew,memo.externeCommandes))
            resultConverter = e => e.buttonData match {
              case  ButtonBar.ButtonData.Apply => true
              case _ => false
            }
            val startStopBtnType = new ButtonType("Ok", ButtonBar.ButtonData.Apply)
            val closeBtnType = new ButtonType("Annuler", ButtonBar.ButtonData.CancelClose)
            dialogPane.value.getButtonTypes.addAll(
              startStopBtnType,closeBtnType
            )
          }


          p.showAndWait() match {
            case Some(value) => if(value.asInstanceOf[Boolean]){
              memo = memo.copy(externeCommandes = cmdnew.value)
              memo.save()
            }
            case None =>
          }


        }
      })
    })
  }
  protected val textFieldJar: TextField = emptyTf(editable = false)
  protected val textFieldRepoGit: TextField = emptyTf()
  protected val buttonJar: Button = bt("Jar") { b =>
    b.onMouseClicked = jarSourceEvent
    b
  }
  protected val textFieldOut: TextField = emptyTf(editable = false)
  protected val buttonOut: Button = bt("Out") { b =>
    b.onMouseClicked = outTarget
    b
  }
  protected val textFieldPom: TextField = emptyTf()
  protected val buttonPom: Button = bt("pom") { b =>
    b.onMouseClicked = pomTarget
    b
  }
  protected val buttonGit: Button = bt("clone") { b =>
    b.onMouseClicked = gitCloneTarget
    b
  }
  protected val buttonLaunch: Button = bt("Launch") { b =>
    b.onMouseClicked = launch
    b
  }
  protected def textFieldGitCmd: TextField = emptyTf()
  protected def buttonGitCmd(textField: TextField)(implicit e : MExtCmd,in : ExterneCommandes): Button = bt("choisir") { b =>
    b.onMouseClicked = gitCmdChoose(textField)
    b
  }

  protected def textFieldMvnCmd: TextField = emptyTf()
  protected def buttonMvnCmd(textField: TextField)(implicit e : MExtCmd,in : ExterneCommandes): Button = bt("choisir") { b =>
    b.onMouseClicked = mvnCmdChoose(textField)
    b
  }

  val mainScene = new Scene {
    fill = Color.AliceBlue
    content = mainContent
  }


  protected val textArea: TextArea = new TextArea {
    margin = Insets(5)
  }

  protected def view(valuep: List[ScanJar.c]): Unit = {
    val ch = valuep.sortWith((a, b) => a.getName < b.getName).map(View).map(_ ())
    rNode.children = ch
    rNode.setExpanded(true)
  }
}
