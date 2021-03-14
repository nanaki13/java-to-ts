package bon.jo.memo

import bon.jo.memo.MemoDBImpl.Entities
import bon.jo.memo.MemoDBImpl.TablesQuery.{keyswords, memos}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future, duration}

class KeyWordDaoImpl(implicit val db: H2Profile.backend.Database) extends Dao[MemoDBImpl.Entities.KeyWord, Int] {

  override def create(a: Entities.KeyWord): FO = {
    db.run(keyswords += a) flatMap {
      r =>
        if (r == 1) {
          db.run(keyswords.filter(_.value === a.value).sortBy(_.id.reverse).result.headOption)
        } else {
          Future.successful(None)
        }
    }
  }
  override def readAll(): FL =  db.run(keyswords.result)
  override type Query = String
  override def findExact(query: Entities.KeyWord): FO = {
    db.run(keyswords.filter(_.value === query.value).result.headOption)
  }
  override def findLike(query:  String): FL = {
    db.run(keyswords.filter(_.value.like(s"%$query%")).result)
  }
  override def update(a: Entities.KeyWord): FO = db.run(keyswords.filter(_.id === a.id).update(a)).map {
    case 1 => Some(a)
    case 0 => None
    case _ => throw new IllegalStateException("plus d'une ligne a été mis a jour")
  }

  override def read(a: Int): FO = db.run(keyswords.filter(_.id === a).result.headOption)

  override def delete(a: Int): FB = db.run(keyswords.filter(_.id === a).delete).map {
    case 1 => true
    case 0 => false
    case _ => throw new IllegalStateException("plus d'une ligne a été supprimer")
  }
}


