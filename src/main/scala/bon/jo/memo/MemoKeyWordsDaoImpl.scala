package bon.jo.memo

import bon.jo.memo.MemoDBImpl.Entities
import bon.jo.memo.MemoDBImpl.TablesQuery.{keyswords, memoKeywords, memos}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{Await, Future, duration}

class MemoKeyWordsDaoImpl(implicit val db: H2Profile.backend.Database,
                          memoDaoImpl: MemoDaoImpl,
                          keyWordDaoImpl: KeyWordDaoImpl) extends Dao[MemoDBImpl.Entities.MemoKeywords, Int] {

  override def create(a: Entities.MemoKeywords): FO = {
   memoDaoImpl.create(a.memo).flatMap {
      case Some(nMemo) =>
        Future.sequence(a.keyWords.map { keyW =>
          keyW.id match {
            case Some(_) => keyWordDaoImpl.update(keyW)
            case None => keyWordDaoImpl.create(keyW)
          }
        }).map(e => {
          val keys = e.flatten
         Some(Entities.MemoKeywords(nMemo,keys))
        })
      case None => Future.successful(None)
    }
  }

  override def update(a: Entities.MemoKeywords,id : Option[Int]): FO ={
    memoDaoImpl.update(a.memo,id).flatMap {
      case Some(nMemo) =>
        Future.sequence(a.keyWords.map { keyW =>
          keyW.id match {
            case Some(_) => keyWordDaoImpl.update(keyW)
            case None => keyWordDaoImpl.create(keyW)
          }
        }).map(e => {
          val keys = e.flatten
          Some(Entities.MemoKeywords(nMemo,keys))
        })
      case None => Future.successful(None)
    }
  }


  override def read(a: Int): FO = {
   val keywWordQuery =  keyswords join memoKeywords on (_.id === _.idKeyWord) filter (_._2.idMemo === a) map(_._1)
    val memo = memoDaoImpl.read(a)
    memo flatMap {
      case Some(value) => db.run(keywWordQuery.result).map(ta =>
        Some(Entities.MemoKeywords(value, ta.toSet)))
      case None => Future.successful(None)
    }
  }

  override def delete(a: Int): FB = db run memoKeywords.filter(_.idMemo === a).delete flatMap {
    _ => memoDaoImpl.delete(a)
  }
}
