package lib.persistence

import ixias.persistence.SlickRepository
import lib.model.Todo
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class TodoRepository[P <: JdbcProfile]()(implicit val driver: P)
    extends SlickRepository[Todo.Id, Todo, P]
    with db.SlickResourceProvider[P] {

  import api._

  /** Get Todo
    */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") { slick =>
      slick.filter(_.id === id).result.headOption
    }

  /** Get All Todo
    * @return
    */
  def getAll(): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(TodoTable, "slave") { slick =>
      slick.result
    }

  /** すべての TODO を取得する
    *
    * TODO のカテゴリも一緒に取得する
    */
  def getAllWithCategory(): Future[Seq[EntityEmbeddedId]] =
    DBAction(TodoTable) { case (db, todoQuery) =>
      DBAction(TodoCategoryTable) { case (_, categoryQuery) =>
        val joinedQuery =
          todoQuery join categoryQuery on (_.categoryId === _.id)

        val action = joinedQuery.result.map(_.map {
          case (todoRecord, categoryRecord) =>
            todoRecord.copy(category = Some(categoryRecord))
        })

        db.run(action)
      }
    }

  /** Add Todo
    */
  override def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(TodoTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  /** Update Todo
    */
  override def update(
      entity: EntityEmbeddedId
  ): Future[Option[EntityEmbeddedId]] = RunDBAction(TodoTable) { slick =>
    val row = slick.filter(_.id === entity.id)
    for {
      old <- row.result.headOption
      _   <- old match {
               case None    => DBIO.successful(0)
               case Some(_) => row.update(entity.v)
             }
    } yield old
  }

  /** Remove Todo
    * @param id
    *   ID
    * @return
    */
  override def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoTable) { slick =>
      val row = slick.filter(_.id === id)
      for {
        old <- row.result.headOption
        _   <- old match {
                 case None    => DBIO.successful(0)
                 case Some(_) => row.delete
               }
      } yield old
    }
}
