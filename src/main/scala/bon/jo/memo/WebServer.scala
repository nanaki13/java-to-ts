package bon.jo.memo

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import org.json4s.DefaultFormats
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api.Database

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends App{
  private implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  private implicit val executionContext: ExecutionContextExecutor = system.executionContext

  private implicit val db: H2Profile.backend.Database = Database.forConfig("h2mem1")
  private implicit object memoDao extends MemoDaoImpl()
  private implicit object keyWordDao extends KeyWordDaoImpl()
  private implicit object memoKeyWordDao extends MemoKeyWordsDaoImpl()
  private implicit val df :DefaultFormats = DefaultFormats
  private val memoRoute = RestRoutes[MemoDBImpl.Entities.Memo]("memo")
  private val keywordRoute = RestRoutes[MemoDBImpl.Entities.KeyWord]("keyword")
  private val memoKeywWordRoute = RestRoutes[MemoDBImpl.Entities.MemoKeywords]("memo-keyword")
  MemoDBImpl.TablesQuery.create.map{
    _ =>
      val routes = concat(memoRoute.routes,keywordRoute.routes,memoKeywWordRoute.routes)
      val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)
      println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
      StdIn.readLine() // let it run until user presses return
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate())
  }






}
