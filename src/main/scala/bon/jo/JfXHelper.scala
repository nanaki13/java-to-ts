package bon.jo

import javafx.event.Event
import javafx.scene.input.MouseEvent
import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Orientation}
import scalafx.scene.Node
import scalafx.scene.control.{Button, Label, SplitPane, TextField}
import scalafx.scene.layout.{HBox, VBox}

trait JfXHelper {
  private val marge: Insets = Insets(5)

  def uiDoLater(f: => Unit): Unit = Platform.runLater {
    f
  }

  def eventDoLater(f: => Unit): javafx.event.EventHandler[MouseEvent] = { _ =>
    uiDoLater(f)
  }

  def event[A <: Event](f: => Unit): javafx.event.EventHandler[A] = { _ => f }

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

  def bt(textS: String)(c: Button => Button): Button = c(new Button {
    text = textS
    margin = marge
  })

  def tf(textS: String)(c: TextField => TextField): TextField = c(new TextField {
    text = textS
    margin = marge
  })

}
