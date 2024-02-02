package lib.model

sealed trait TodoState

object TodoState {
  case object Todo       extends TodoState
  case object InProgress extends TodoState
  case object Done       extends TodoState

  def from(value: Byte): TodoState = {
    value match {
      case 0 => Todo
      case 1 => InProgress
      case 2 => Done
      case _ => throw new Exception(s"Invalid value: ${value}")
    }
  }

  def toByte(value: TodoState): Byte = {
    value match {
      case Todo       => 0
      case InProgress => 1
      case Done       => 2
    }
  }
}
