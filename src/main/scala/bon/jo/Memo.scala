package bon.jo

import bon.jo.jtots.ui.ExterneCommandes

case class Memo(lastDir: Option[String] = None
                , lastJat: Option[String] = None,
                externeCommandes: ExterneCommandes = ExterneCommandes() )
