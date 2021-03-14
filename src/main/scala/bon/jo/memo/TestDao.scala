package bon.jo.memo

object TestDao extends App {
  implicit val matcher: Dao.Id[String] = e => e

  import scala.concurrent.ExecutionContext.Implicits._

  val daoString = new Dao.ListDao[String, String]() {
    override type Query = String => Boolean

    override def findLike(query: String => Boolean): FL = Dao.okFuture {
      listBuffer.filter(query).toList
    }
  }
  daoString + "s1"
  (daoString --> "s1") foreach (println)
  (daoString - "s1")
  daoString --> "s1" foreach (println)
}
