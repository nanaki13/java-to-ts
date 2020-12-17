package bon.jo

import bon.jo.TestMapping.A
import bon.jo.WebServiceClient.Impl

import scala.concurrent.Future

trait Mappeur[I,O] {
  def apply(i : I): O
}


object Mappeur {

  implicit class obJMap[A](a: A){
    def mapTo[B](implicit mappeur : Mappeur[A,B]): B ={
      mappeur(a)
    }
  }


}
object TestMapping extends App{
  import Mappeur._
  object Mapping{
    implicit val mappeurO : Mappeur[C,D]= a => D(a=a.a,b= a.b)
    implicit val mappeur : Mappeur[A,B]= a => B(a=a.a,b= a.b,d = a.c.mapTo)
  }

  import Mapping._
  case class A(a : String,b : String,c : C)
  case class B(a : String,b : String,d : D)
  case class C(a : String,b : String)
  case class D(a : String,b : String)

  val a: A = A(",","",C("",""))

  val b: B = a.mapTo

  println(b)
}

trait WebServiceClient[A]{

  type Id
  def get(a : Id):Future[A]
  def post(a : A):Future[A]
  def patch(a : A):Future[A]
  def delete(a : A):Future[A]
}

object WebServiceClient{
  object Impl extends WebServiceClient[A] {
    override type Id = String

    override def get(a: String): Future[A] = ???

    override def post(a: A): Future[A] = ???

    override def patch(a: A): Future[A] = ???

    override def delete(a: A): Future[A] = ???
  }
}
