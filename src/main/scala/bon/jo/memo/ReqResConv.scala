package bon.jo.memo

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller, ToResponseMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse, RequestEntity}
import akka.http.scaladsl.unmarshalling.{FromRequestUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import org.json4s.DefaultFormats

trait ReqResConv[A] {
  implicit val formats: DefaultFormats
  implicit val materializer: Materializer
  implicit val manifest: Manifest[A]

  implicit val toEntityMarshaller : ToEntityMarshaller[A] = Marshaller
    .opaque[A, RequestEntity](
      resp => {
       HttpEntity(contentType = ContentTypes.`application/json`, string = org.json4s.native.Serialization.write(resp))
      })

  implicit val toEntityMarshallerSeq : ToEntityMarshaller[Seq[A]] = Marshaller
    .opaque[Seq[A], RequestEntity](
      resp => {
        HttpEntity(contentType = ContentTypes.`application/json`, string = org.json4s.native.Serialization.write(resp))
      })
  // marshalling would usually be derived automatically using libraries
  implicit val fromRequestUnmarshaller: FromRequestUnmarshaller[A] = {
    Unmarshaller[HttpRequest, A](implicit ec => memoRequest =>
      memoRequest.entity.dataBytes.runFold("")((res, bs) => s"$res${bs.utf8String}").map(org.json4s.native.Serialization.read[A]))
  }
  /*implicit val toResponseMarshaller: ToResponseMarshaller[A] = {
    Marshaller
      .withFixedContentType[A, HttpResponse](
        contentType = ContentTypes.`application/json`)(
        resp => {
          HttpResponse(entity = HttpEntity(contentType = ContentTypes.`application/json`, string = org.json4s.native.Serialization.write(resp)))
        })
  }*/
/*  implicit val toResponseMarshallerSeq: ToResponseMarshaller[Seq[A]] = Marshaller
    .withFixedContentType[Seq[A], HttpResponse](
      contentType = ContentTypes.`application/json`)(
      resp => {
        HttpResponse(entity = HttpEntity(contentType = ContentTypes.`application/json`, string = org.json4s.native.Serialization.write(resp)))
      })*/
}
