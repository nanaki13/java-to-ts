package bon.jo

import bon.jo.jtots.core.SerPers
import bon.jo.jtots.core.SerPers._
import ImplicitDef._
import bon.jo.jtots.config.AllConfig.AppDir

import scala.util.{Failure, Success, Try}

class HaveOneMemo(implicit val appDir : AppDir) {
  implicit val idMemo: IdString[Memo] = _ => "memo"
  implicit val serpser : SerPers[Memo] = create
  var memo: Memo = {
    Try(Memo().restore()) match {
      case Failure(exception) => Memo()
      case Success(value) => value match {
        case Some(value) => value
        case None => Memo()
      }
    }
  }
}
