package bon.jo.memo

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, HttpMethod, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import bon.jo.memo.MemoDBImpl.Entities
import org.json4s.DefaultFormats
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api.Database

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.io.StdIn

object WebServer extends App{
  private implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  private implicit val executionContext: ExecutionContextExecutor = system.executionContext

  private implicit val db: H2Profile.backend.Database = Database.forConfig("h2mem1")
  private implicit object memoDao extends MemoDaoImpl()
  private implicit val df :DefaultFormats = DefaultFormats
  private val memoRoute = RestRoutes[MemoDBImpl.Entities.Memo]("memo")

  MemoDBImpl.TablesQuery.create
  private val bindingFuture = Http().newServerAt("localhost", 8080).bind(memoRoute.routes)

  val fut = memoDao create Entities.Memo(None, "toto") flatMap {
    case None => Future.failed(new IllegalStateException())
    case Some(value) => {
      println(value)
      memoDao findLike "t%" map {
        case Nil => println("rien")
        case a => println(a)
      }
    }
  }

 val req =  HttpRequest(HttpMethods.POST,"http://localhost:8080/memo"
    ,entity = HttpEntity(org.json4s.native.Serialization.write(MemoDBImpl.Entities.Memo(None,"test post"))))

 val f1 =  Http().singleRequest(req).map(println)
  f1.flatMap(e=>{
    Http().singleRequest(HttpRequest(HttpMethods.GET,"http://localhost:8080/memo"))
      .map(_.entity)
      .flatMap(_.dataBytes.map(_.utf8String)
        .runFold("")(_ + _))
      .map(println)
  })
  import memoRoute.toEntityMarshallerSeq

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate())
}
