package bon.jo.memo

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToResponseMarshaller}
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import org.json4s.DefaultFormats
import slick.jdbc.H2Profile

import scala.concurrent.Future
import scala.util.{Failure, Success}


trait RestRoutes[A] extends ReqResConv[A] with CORSHandler {


  implicit val db: H2Profile.backend.Database
  implicit val dao: Dao[A, Int]
  implicit val marshallerBoolean: ToResponseMarshaller[Boolean] = Marshaller.strict(b => Marshalling.Opaque {
    () =>
      if (b) {
        HttpResponse(StatusCodes.NoContent)
      } else {
        HttpResponse(StatusCodes.NotFound)
      }
  })
  val name: String

  def resolve[B](f: => Future[B])(implicit _marshaller: ToResponseMarshaller[B]): Route = {
    onComplete {
      f
    } {
      case Success(value) => complete(value)
      case Failure(ex) => complete(InternalServerError, s"An error occurred: ${ex.getMessage}")
    }
  }

  val routes: Route = corsHandler {

    pathPrefix(name) {
      concat(path(IntNumber) { id =>
        concat(get {
          resolve {
            dao.read(id)
          }
        }, delete {
          resolve {
            dao.delete(id)
          }
        }, patch {
          decodeRequest {
            entity(as[A]) { order =>
              resolve {
                dao.update(order,Option(id))
              }
            }
          }
        })
      }
        , get {
          resolve {
            dao.readAll()
          }
        },patch {
          decodeRequest {
            entity(as[A]) { order =>
              resolve {
                dao.update(order)
              }
            }
          }
        },
        post {
          decodeRequest {
            entity(as[A]) { order =>
              resolve {
                dao.create(order)
              }
            }
          }
        })
    }
  }
}

object RestRoutes {

  case class RestRoutesImpl[A](name: String)(implicit val db: H2Profile.backend.Database,
                                             val dao: Dao[A, Int],
                                             val formats: DefaultFormats,
                                             val materializer: Materializer, val manifest: Manifest[A]
  ) extends RestRoutes[A]

  def apply[A](name: String)(implicit db: H2Profile.backend.Database, dao: Dao[A, Int], formats: DefaultFormats, materializer: Materializer, manifest: Manifest[A]): RestRoutes[A] = RestRoutesImpl(name)
}

