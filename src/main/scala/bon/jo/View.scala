package bon.jo

import bon.jo.ScanJar.c
import scalafx.scene.control.TreeItem

case class View(c: c) {
  def apply(): TreeItem[View] = new TreeItem[View](this)

  override def toString: String = c.getName
}
