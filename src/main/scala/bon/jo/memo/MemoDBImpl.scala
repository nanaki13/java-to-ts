package bon.jo.memo
import bon.jo.memo.MemoDBImpl.Entities._
import bon.jo.memo.MemoDBImpl.Schema.{KeysWords, Memos, _}
import bon.jo.memo.MemoDBImpl.TablesQuery._
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
object MemoDBImpl {
  object Entities{
    case class Memo(id: Option[Int],title : String, content: String)
    case class KeyWord(id: Option[Int], value: String)
    case class MemoKeywordRel(memo: Int, keyWord: Int)
    case class MemoKeywords(memo: Memo, keyWords: Set[KeyWord])
  }
  object Schema{

    class Memos(tag: Tag) extends Table[Memo](tag, "MEMO") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
      def content = column[String]("CONTENT")
      def title = column[String]("TITLE")
      def * = (id.?,title, content) <> (Memo.tupled, Memo.unapply)
      def idx = index("idx_content", content, unique = true)
    }

    class KeysWords(tag: Tag) extends Table[KeyWord](tag, "KEYS_WORD") {
      def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
      def value = column[String]("VALUE")

      def * = (id.?, value) <> (KeyWord.tupled, KeyWord.unapply)
      def idx = index("idx_value", value, unique = true)
    }


    class MemoKeywordsTable(tag: Tag) extends Table[MemoKeywordRel](tag, "MEMO_KEYS_WORD") {

      def idMemo = column[Int]("ID_MEMO")
      def idKeyWord = column[Int]("ID_KEY_WORD")
      def memoFk = foreignKey("MEMO_FK", idMemo, memos)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
      def keyWordFk = foreignKey("KEYWORD_FK", idMemo, keyswords)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
      def * = (idMemo, idKeyWord) <> (MemoKeywordRel.tupled, MemoKeywordRel.unapply)

    }
  }



  object TablesQuery{
    val memos = TableQuery[Memos]
    val keyswords = TableQuery[KeysWords]
    val memoKeywords = TableQuery[MemoKeywordsTable]

    val allinOrder = List(memos,keyswords,memoKeywords)

    def create(implicit db: H2Profile.backend.Database,executionContext: ExecutionContext) = Future.sequence(  allinOrder.map(_.schema.createIfNotExists).map(db.run))
  }




  def keysWord(memo: Memo) = {
    memoKeywords.filter(_.idMemo === memo.id )
  }

}
