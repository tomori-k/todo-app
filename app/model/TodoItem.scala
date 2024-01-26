package model

case class TodoItem(
    title:    String,
    body:     String,
    state:    TodoState,
    category: TodoCategory
)
