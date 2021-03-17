package bon.jo.memo

import bon.jo.memo.MemoDBImpl.Entities
import slick.jdbc.H2Profile

import scala.concurrent.{Await, ExecutionContext, Future, duration}

object test extends App {
  implicit val db: H2Profile.backend.Database = H2Profile.api.Database.forConfig("h2mem1")
  def run(implicit executionContext: ExecutionContext): Unit ={
    val dao = new MemoDaoImpl()

    MemoDBImpl.TablesQuery.create

    val fut = dao create Entities.Memo(None, "toto", "toto") flatMap {
      case None => Future.failed(new IllegalStateException())
      case Some(value) => dao create Entities.Memo(None, "toto", "toto") map {
        case None =>
        case Some(value) => println(value)
      }
    }

    Await.result(fut, duration.Duration.Inf)
  }


}
