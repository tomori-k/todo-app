package lib.model

import ixias.model._
import lib.model.Todo.Id

import java.time.LocalDateTime

case class Todo(
    id:         Option[Id],
    categoryId: TodoCategory.Id,
    title:      String,
    body:       String,
    state:      TodoState,
    updatedAt:  LocalDateTime = NOW,
    createdAt:  LocalDateTime = NOW
) extends EntityModel[Id]

object Todo {
  val Id = the[Identity[Id]]
  type Id = Long @@ Todo
}
