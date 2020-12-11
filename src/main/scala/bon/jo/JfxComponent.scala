package bon.jo

import scalafx.geometry.Insets
import scalafx.scene.control._




trait JfxComponent extends JfXHelper {
  self: JfxEvent =>



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


  protected val textArea: TextArea = new TextArea {
    margin = Insets(5)
  }

  protected def view(valuep: List[ScanJar.c]): Unit = {
    val ch = valuep.sortWith((a, b) => a.getName < b.getName).map(View).map(_ ())
    rNode.children = ch
    rNode.setExpanded(true)
  }
}
