package lib.model

import ixias.model._
import ixias.util.EnumStatus
import lib.model.Todo.Id

import java.time.LocalDateTime

case class Todo(
    id:         Option[Id],
    categoryId: TodoCategory.Id,
    title:      String,
    body:       String,
    state:      Todo.TodoState,
    updatedAt:  LocalDateTime        = NOW,
    createdAt:  LocalDateTime        = NOW,
    category:   Option[TodoCategory] = None
) extends EntityModel[Id]

object Todo {
  val Id = the[Identity[Id]]
  type Id = Long @@ Todo

  sealed abstract class TodoState(val code: Short, val name: String)
      extends EnumStatus

  object TodoState extends EnumStatus.Of[TodoState] {
    case object Todo       extends TodoState(code = 0, name = "TODO(着手前)")
    case object InProgress extends TodoState(code = 1, name = "進行中")
    case object Done       extends TodoState(code = 2, name = "完了")
  }
}
