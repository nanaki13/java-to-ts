package bon.jo

case class Memo(lastDir: Option[String] = None
                , lastJat: Option[String] = None,externeCommandes: ExterneCommandes = ExterneCommandes() )
