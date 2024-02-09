package lib.model

import ixias.model._
import lib.model.TodoCategory.Id

import java.time.LocalDateTime

case class TodoCategory(
    id:        Option[Id],
    name:      String,
    color:     String,
    updatedAt: LocalDateTime = NOW,
    createdAt: LocalDateTime = NOW
) extends EntityModel[Id]

object TodoCategory {
  val Id = the[Identity[Id]]
  type Id = Long @@ TodoCategory
}
