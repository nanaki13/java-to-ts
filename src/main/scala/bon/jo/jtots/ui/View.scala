package bon.jo.jtots.ui

import bon.jo.jtots.core.ProcessClass.c
import scalafx.scene.control.TreeItem

/**
 * Class view as  [[TreeItem]]
 *
 * @param c : the class
 */
case class View(c: c) {
  /**
   * Create the [[TreeItem]]
   *
   * @return
   */
  def apply(): TreeItem[View] = new TreeItem[View](this)

  /**
   * Rendering used by the [[CellFactory]]
   *
   * @return
   */
  override def toString: String = c.getName
}
