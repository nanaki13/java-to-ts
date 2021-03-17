package bon.jo.memo

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, RequestEntity}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromRequestUnmarshaller, FromResponseUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import org.json4s.DefaultFormats

import scala.concurrent.{ExecutionContext, Future}

trait ReqResConv[A] {
  implicit val formats: DefaultFormats
  implicit val materializer: Materializer
  implicit val manifest: Manifest[A]


  def entityConv[B](a : B)(implicit  manifest: Manifest[B]): HttpEntity.Strict = {
    HttpEntity(contentType = ContentTypes.`application/json`, string = org.json4s.native.Serialization.write(a))
  }
  def fromentity[B](entity : HttpEntity)(implicit  manifest: Manifest[B],executionContext: ExecutionContext):Future[B]= {
    entity.dataBytes.runFold("")((res, bs) => s"$res${bs.utf8String}").map(org.json4s.native.Serialization.read[B])
  }
  implicit val toEntityMarshaller : ToEntityMarshaller[A] = Marshaller
    .opaque[A, RequestEntity](
      resp => {
        entityConv(resp)
      })

  implicit val toEntityMarshallerSeq : ToEntityMarshaller[Seq[A]] = Marshaller
    .opaque[Seq[A], RequestEntity](
      resp => {
        entityConv(resp)
      })
  // marshalling would usually be derived automatically using libraries
  implicit val fromRequestUnmarshaller: FromEntityUnmarshaller[A] = {
    Unmarshaller[HttpEntity, A](implicit ec => memoRequest =>
      fromentity[A](memoRequest))
  }
  implicit val fromRequestUnmarshallerSeq: FromEntityUnmarshaller[Seq[A]] = {
    Unmarshaller[HttpEntity, Seq[A]](implicit ec => memoRequest =>
      fromentity[Seq[A]](memoRequest))
  }
}
