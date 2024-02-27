package lib.persistence.db

import ixias.persistence.model.{DataSourceName, Table}
import lib.model.TodoCategory
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime

case class TodoCategoryTable[P <: JdbcProfile]()(implicit val driver: P)
    extends Table[TodoCategory, P] {

  import api._

  lazy val dsn: Map[String, DataSourceName] = Map(
    "master" -> DataSourceName("ixias.db.mysql://master/to_do"),
    "slave"  -> DataSourceName("ixias.db.mysql://slave/to_do")
  )

  class Query extends BasicQuery(new Table(_)) {}
  lazy val query = new Query

  class Table(tag: Tag) extends BasicTable(tag, "to_do_category") {
    import lib.model.TodoCategory._

    // Columns
    /* @1 */
    def id        = column[Id]("id", O.UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */
    def name      = column[String]("name", O.Utf8Char255)
    /* @3 */
    def color     = column[String]("color", O.Utf8Char8)
    /* @4 */
    def updatedAt = column[LocalDateTime]("updated_at", O.TsCurrent)
    /* @5 */
    def createdAt = column[LocalDateTime]("created_at", O.Ts)

    type TableElementTuple = (
        Option[Id],
        String,
        String,
        LocalDateTime,
        LocalDateTime
    )

    // DB <=> Scala の相互のmapping定義
    def * = (id.?, name, color, updatedAt, createdAt) <> (
      // Tuple(table) => Model
      (t: TableElementTuple) =>
        TodoCategory(
          t._1,
          t._2,
          t._3,
          t._4,
          t._5
        ),
      // Model => Tuple(table)
      (v: TableElementType) =>
        TodoCategory.unapply(v).map { t =>
          (
            t._1,
            t._2,
            t._3,
            LocalDateTime.now(),
            t._5
          )
        }
    )
  }
}
