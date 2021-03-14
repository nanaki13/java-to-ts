package bon.jo.memo

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToResponseMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import org.json4s.DefaultFormats
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait RestRoutes[A] extends ReqResConv[A] {



  implicit val db: H2Profile.backend.Database
  implicit val dao  : Dao[A, Int]
  val name : String

  val routes: Route = {

    path(name) {
      concat(get {
        complete {
          println("read all")
          dao.readAll()
        }
      }
        , post {
          decodeRequest {
            // unmarshal with in-scope unmarshaller
            entity(as[A]) { order =>
              complete {
                dao.create(order)
              }
            }
          }
        })
    }
  }
}
object RestRoutes{
  case class RestRoutesImpl[A](name : String)(implicit val db: H2Profile.backend.Database,
                                              val dao  : Dao[A, Int],
                                              val formats: DefaultFormats,
                                              val materializer: Materializer,val manifest: Manifest[A]
  ) extends RestRoutes[A]
  def apply[A](name : String)(implicit db: H2Profile.backend.Database,dao  : Dao[A, Int],formats: DefaultFormats,materializer: Materializer, manifest: Manifest[A]): RestRoutes[A] = RestRoutesImpl(name)
}

