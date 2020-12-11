package bon.jo

import bon.jo.SerPers.ImplicitDef.create
import bon.jo.SerPers.{IdString, SerObject}

import scala.util.{Failure, Success, Try}

trait HaveOneMemo {
  implicit val idMemo: IdString[Memo] = _ => "memo"

  var memo: Memo = {
    Try(Memo().restore()) match {
      case Failure(exception) => Memo()
      case Success(value) => value
    }
  }
}
