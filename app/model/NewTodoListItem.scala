package model

import play.api.libs.json.Json

//Data Transfer Object (DTO)
case class NewTodoListItem(description: String)

object NewTodoListItem {
  implicit val newTodoListItemJson = Json.format[NewTodoListItem]
}
