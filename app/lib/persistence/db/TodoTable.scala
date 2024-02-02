package lib.persistence.db

import ixias.persistence.model.{DataSourceName, Table}
import lib.model.Todo
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime

case class TodoTable[P <: JdbcProfile]()(implicit val driver: P)
    extends Table[Todo, P] {

  import api._

  lazy val dsn: Map[String, DataSourceName] = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/to_do"),
    "slave"  -> DataSourceName("ixias.db.mysql://slave/to_do")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "to_do") {
    import lib.model._

    // Columns
    /* @1 */
    def id         = column[Todo.Id]("id", O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */
    def categoryId = column[Long]("category_id", O.UInt64)
    /* @3 */
    def title      = column[String]("title", O.Utf8Char255)
    /* @4 */
    def body       = column[String]("body", O.Text)
    /* @5 */
    def state      = column[Byte]("state", O.UInt8)
    /* @6 */
    def updatedAt  = column[LocalDateTime]("updated_at", O.TsCurrent)
    /* @7 */
    def createdAt  = column[LocalDateTime]("created_at", O.Ts)

    type TableElementTuple = (
        Option[Todo.Id],
        Long,
        String,
        String,
        Byte,
        LocalDateTime,
        LocalDateTime
    )

    // DB <=> Scala の相互のmapping定義
    def * = (id.?, categoryId, title, body, state, updatedAt, createdAt) <> (
      // Tuple(table) => Model
      (t: TableElementTuple) =>
        Todo(
          t._1,
          t._2,
          t._3,
          t._4,
          TodoState.from(t._5),
          t._6,
          t._7
        ),
      // Model => Tuple(table)
      (v: TableElementType) =>
        Todo.unapply(v).map { t =>
          (
            t._1,
            t._2,
            t._3,
            t._4,
            TodoState.toByte(t._5),
            LocalDateTime.now(),
            t._7
          )
        }
    )
  }
}
