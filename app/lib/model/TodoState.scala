package lib.model

import ixias.util.EnumStatus

sealed abstract class TodoState(val code: Short, val name: String)
    extends EnumStatus {
  
  import lib.model.TodoState._

  def apply(code: Short): TodoState = {
    code match {
      case 0 => Todo
      case 1 => InProgress
      case 2 => Done
      case _ => throw new Exception("No such a state code")
    }
  }
}

object TodoState extends EnumStatus.Of[TodoState] {
  case object Todo       extends TodoState(code = 0, name = "TODO(着手前)")
  case object InProgress extends TodoState(code = 1, name = "進行中")
  case object Done       extends TodoState(code = 2, name = "完了")
}
