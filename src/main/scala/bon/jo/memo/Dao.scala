package bon.jo.memo

import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

trait Dao[A,ID] {
  type FO = Future[Option[A]]
  type FL= Future[Seq[A]]
  type FB = Future[Boolean]
  type Query
  def create(a: A): FO

  def update(a: A): FO

  def read(a: ID): FO
  def readAll(): FL

  def delete(a: ID): FB

  def findLike(query: Query  ) : FL
  def findExact(query: A  ) : FO

  def +(a: A): FO = create(a)

  def -(a: ID): FB = delete(a)

  def -->(a: ID): FO = read(a)

  def <--(a: A): FO = update(a)
}



object Dao {
  trait Id[A] {
    def id(a: A): Any
  }

  object Id {

    implicit class UsingMatcher[A: Id](a: A) {
      def id : Any = implicitly[Id[A]].id(a)
      def ===(b: A): Boolean = {
        a.id == b.id
      }
    }

  }
  case class DelegateDao[A,ID](dao: Dao[A,ID]) extends Dao[A,ID] {


    override def create(a: A) : FO = dao.create(a)

    override def update(a: A): FO= dao.update(a)

    override def read(a: ID): FO = dao.read(a)

    override def delete(a: ID): FB = dao.delete(a)

    override def findLike(query: Query): FL = dao.findLike(query.asInstanceOf[dao.Query])

    override def findExact(query: A): FO = dao.findExact(query)

    override def readAll(): FL = dao.readAll()
  }

  def okFuture[A](a : A): Future[A] = Future.successful(a)
  abstract  class ListDao[A: Id,ID]() extends Dao[A,ID] {
    protected val listBuffer: ListBuffer[A] = ListBuffer()
    import Id._

    override def create(a: A): FO =okFuture{
      listBuffer.find(_ === a) match {
        case Some(value) => None
        case None =>
          listBuffer += a
          Option(a)
      }

    }
    override def update(a: A): FO = okFuture{
      listBuffer.zipWithIndex.find(_._1 === a).map(e => {
        listBuffer.remove(e._2)
        listBuffer.insert(e._2, a)
        a
      })
    }

    override def read(a: ID): FO = okFuture(listBuffer.find(_.id == a))

    override def readAll(): FL = okFuture(listBuffer.toSeq)

    override def delete(a: ID): FB = okFuture(listBuffer.zipWithIndex.find(_._1.id == a).map(e => {
      listBuffer.remove(e._2)
      a
    }) match {
      case Some(value) => true
      case None => false
    })

    override def findExact(query: A): FO = okFuture (listBuffer.find(_ == query))
  }



}
