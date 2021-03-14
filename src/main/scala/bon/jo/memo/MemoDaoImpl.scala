package bon.jo.memo

import bon.jo.memo.MemoDBImpl.Entities
import bon.jo.memo.MemoDBImpl.TablesQuery.memos
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future, duration}

class MemoDaoImpl(implicit val db: H2Profile.backend.Database) extends Dao[MemoDBImpl.Entities.Memo, Int] {

  override def create(a: Entities.Memo): FO = {
    db.run(memos += a) flatMap {
      r =>
        if (r == 1) {
          db.run(memos.filter(_.content === a.content).sortBy(_.id.reverse).result.headOption)
        } else {
          Future.successful(None)
        }
    }
  }

  override def update(a: Entities.Memo): FO = db.run(memos.filter(_.id === a.id).update(a)).map {
    case 1 => Some(a)
    case 0 => None
    case _ => throw new IllegalStateException("plus d'une ligne a été mis a jour")
  }

  override def read(a: Int): FO = db.run(memos.filter(_.id === a).result.headOption)

  override def delete(a: Int): FB = db.run(memos.filter(_.id === a).delete).map {
    case 1 => true
    case 0 => false
    case _ => throw new IllegalStateException("plus d'une ligne a été supprimer")
  }

  override type Query = String

  override def findLike(query:  String): FL = {
    db.run(memos.filter(_.content.like(query)).result)
  }

  override def findExact(query: Entities.Memo): FO = {
    db.run(memos.filter(_.content === query.content).result.headOption)
  }

  override def readAll(): FL =  db.run(memos.result)
}


