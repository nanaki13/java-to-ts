package bon.jo.memo

import bon.jo.memo.MemoDBImpl.Entities
import slick.jdbc.H2Profile

import scala.concurrent.{Await, ExecutionContext, Future, duration}

object test2 extends App {
  implicit val db: H2Profile.backend.Database = H2Profile.api.Database.forConfig("h2mem1")
  val dao = new KeyWordDaoImpl()
  def run(implicit executionContext: ExecutionContext): Unit ={
    MemoDBImpl.TablesQuery.create

    val fut = dao create Entities.KeyWord(None, "toto") flatMap {
      case None => Future.failed(new IllegalStateException())
      case Some(value) => {
        println(value)
        dao findLike "t" map {
          case Nil => println("rien")
          case a => println(a)
        }
      }
    }
    Await.result(fut, duration.Duration.Inf)
  }



}
