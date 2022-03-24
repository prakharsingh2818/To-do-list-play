package controllers

import akka.util.ByteString
import com.google.inject.{Inject, Singleton}
import model.{NewTodoListItem, TodoListItem}
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc._

import scala.collection.mutable.ListBuffer

@Singleton
class TodoListController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  private val todoList = ListBuffer[TodoListItem]()
  todoList += TodoListItem(1, "Learning Scala", isItDone = true)
  todoList += TodoListItem(2, "Learning Akka", isItDone = false)
  todoList += TodoListItem(3, "Learning Play", isItDone = false)
  implicit val todoListJson: OFormat[TodoListItem] = Json.format[TodoListItem]

  def getAll: Action[AnyContent] = Action {
    if (todoList.isEmpty) NoContent
    else Ok(Json.toJson(todoList))
  }

  def getById(itemId: Long): Action[AnyContent] = Action {
    val foundItem = todoList.find(_.id == itemId)
    foundItem match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  def markAsDone(itemId: Long): Action[AnyContent] = Action {
    val itemToUpdate = todoList.find(_.id == itemId)
    itemToUpdate match {
      case Some(item) =>
        todoList.remove(todoList.indexOf(item))
        todoList += item.copy(isItDone = true)
        Ok(Json.toJson(todoList.find(_.id == itemId)))
      case None => NotFound
    }
  }

  def deleteAllDone(): Action[AnyContent] = Action {
    val itemsToBeDeleted = todoList.filter(_.isItDone)
    itemsToBeDeleted match {
      case list: ListBuffer[TodoListItem] =>
        list.map { item =>
          todoList.remove(todoList.indexOf(item))
        }
        Ok
      case _ => Result(header = ResponseHeader(200), body = HttpEntity.Strict(ByteString("No data deleted"), contentType = Option(CONTENT_TYPE)))
    }
  }

  def addNewItem(): Action[AnyContent] = Action { implicit request =>
    val content: AnyContent = request.body
    val jsonResult: Option[JsValue] = content.asJson
    val todoListItem: Option[NewTodoListItem] = jsonResult.flatMap { result: JsValue =>
      Json.fromJson[NewTodoListItem](result).asOpt
    }
    todoListItem match {
      case Some(item) =>
        val nextId = todoList.map(_.id).max + 1
        val newItem = TodoListItem(nextId, item.description, isItDone = false)
        todoList += newItem
        Created(Json.toJson(newItem))
      case None => BadRequest
    }
  }
}
