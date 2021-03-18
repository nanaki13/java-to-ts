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


  override def readAll(): FL = {
    (for (a <- (for {
      memos <- memoDaoImpl.readAll()
    } yield {
      for {
        m <- memos
      } yield {
        val q = (keyswords join memoKeywords on (_.id === _.idKeyWord)).filter(_._2.idMemo === m.id).map(_._1).result
        for (f <- db.run(q)) yield Entities.MemoKeywords(m, f.toSet)
      }
    })) yield Future.sequence(a)).flatten
  }

  override def findExact(query: Entities.MemoKeywords): FO = ???

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
          Some(Entities.MemoKeywords(nMemo, keys))
        })
      case None => Future.successful(None)
    }
  }

  override def update(a: Entities.MemoKeywords, id: Option[Int]): FO = {
    memoDaoImpl.update(a.memo, id).flatMap {
      case Some(nMemo) =>
        Future.sequence(a.keyWords.map { keyW =>
          keyW.id match {
            case Some(_) => keyWordDaoImpl.update(keyW)
            case None => keyWordDaoImpl.create(keyW)
          }
        }).map(e => {
          val keys = e.flatten
          Some(Entities.MemoKeywords(nMemo, keys))
        })
      case None => Future.successful(None)
    }
  }


  override def read(a: Int): FO = {
    val keywWordQuery = keyswords join memoKeywords on (_.id === _.idKeyWord) filter (_._2.idMemo === a) map (_._1)
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

  override type Query = String

  override def findLike(query: String): FL = {

    def joinQ = keyswords join
      memoKeywords on
      (_.id === _.idKeyWord) join
      memos on (_._2.idMemo === _.id)

    def filter = joinQ filter {
      e => e._1._1.value.like(s"%$query%")
    }

    def mapDistinct = (filter map (_._2)).distinct

    (db.run(mapDistinct.result)) flatMap { memo =>
      Future.sequence {
        for (mm <- memo) yield {
          val q = memoKeywords join keyswords on
            ((m, k) => m.idKeyWord === k.id && m.idMemo === mm.id) map
            (_._2)
          db.run(q.result) map { kws => Entities.MemoKeywords(mm, kws.toSet) }
        }
      }
    }
  }
}
