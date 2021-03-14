//package bon.jo.memo
//
//import bon.jo.memo.MemoDBImpl.Entities
//import bon.jo.memo.MemoDBImpl.TablesQuery.{keyswords, memos}
//import slick.jdbc.H2Profile
//import slick.jdbc.H2Profile.api._
//
//import scala.concurrent.ExecutionContext.Implicits._
//import scala.concurrent.{Await, Future, duration}
//
//class MemoKeyWordsDaoImpl(implicit val db: H2Profile.backend.Database,
//                          memoDaoImpl: MemoDaoImpl,
//                          keyWordDaoImpl: MemoKeyWordsDaoImpl) extends Dao[MemoDBImpl.Entities.MemoKeywords, Int] {
//
//  override def create(a: Entities.MemoKeywords): FO = {
//    memoDaoImpl.create( a.memo).map(nMemo => {
//      a.keyWords.map { keyW =>
//    //    keyWordDaoImpl.
//      }
//    })
//
//    db.run(keyswords += a) flatMap {
//      r =>
//        if (r == 1) {
//          db.run(keyswords.filter(_.value === a.value).sortBy(_.id.reverse).result.headOption)
//        } else {
//          Future.successful(None)
//        }
//    }
//  }
//
//  override def update(a: Entities.KeyWord): FO = db.run(keyswords.filter(_.id === a.id).update(a)).map {
//    case 1 => Some(a)
//    case 0 => None
//    case _ => throw new IllegalStateException("plus d'une ligne a été mis a jour")
//  }
//
//  override def read(a: Int): FO = db.run(keyswords.filter(_.id === a).result.headOption)
//
//  override def delete(a: Int): FB = db.run(keyswords.filter(_.id === a).delete).map {
//    case 1 => true
//    case 0 => false
//    case _ => throw new IllegalStateException("plus d'une ligne a été supprimer")
//  }
//}
//
//object test2 extends App {
//  implicit val db: H2Profile.backend.Database = Database.forConfig("h2mem1")
//  val dao = new MemoKeyWordsDaoImpl()
//
//  MemoDBImpl.TablesQuery.create
//
//  val fut = dao create Entities.KeyWord(None, "toto") flatMap  {
//    case None => Future.failed(new IllegalStateException())
//    case Some(value) => dao create Entities.KeyWord(None, "totdsqdo") map {
//      case None =>
//      case Some(value) => println(value)
//    }
//  }
//
//  Await.result(fut, duration.Duration.Inf)
//
//}
