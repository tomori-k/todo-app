package lib.persistence

import ixias.persistence.SlickRepository
import lib.model.TodoCategory
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class TodoCategoryRepository[P <: JdbcProfile]()(implicit val driver: P)
    extends SlickRepository[TodoCategory.Id, TodoCategory, P]
    with db.SlickResourceProvider[P] {

  import api._

  /** Get TodoCategory
    */
  def get(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoCategoryTable, "slave") { slick =>
      slick.filter(_.id === id).result.headOption
    }

  /** Get All Categories
    * @return
    */
  def getAll(): Future[Seq[EntityEmbeddedId]] =
    RunDBAction(TodoCategoryTable, "slave") { slick =>
      slick.result
    }

  /** Add TodoCategory
    */
  override def add(entity: EntityWithNoId): Future[Id] =
    RunDBAction(TodoCategoryTable) { slick =>
      slick returning slick.map(_.id) += entity.v
    }

  /** Update TodoCategory
    */
  override def update(
      entity: EntityEmbeddedId
  ): Future[Option[EntityEmbeddedId]] = RunDBAction(TodoCategoryTable) {
    slick =>
      val row = slick.filter(_.id === entity.id)
      for {
        old <- row.result.headOption
        _   <- old match {
                 case None    => DBIO.successful(0)
                 case Some(_) => row.update(entity.v)
               }
      } yield old
  }

  /** Remove TodoCategory
    * @param id
    *   ID
    * @return
    */
  override def remove(id: Id): Future[Option[EntityEmbeddedId]] =
    RunDBAction(TodoCategoryTable) { slick =>
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
