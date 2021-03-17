package bon.jo.memo

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling, ToResponseMarshallable, ToResponseMarshaller}
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.http.scaladsl.model.{ContentTypes, HttpCharset, HttpEntity, HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import org.json4s.DefaultFormats
import slick.jdbc.H2Profile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}


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

