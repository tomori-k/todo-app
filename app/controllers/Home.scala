/** to do sample project
  */

package controllers

import lib.model.{Todo, TodoCategory, TodoState}
import model.ViewValueHome
import play.api.data.Form
import play.api.data.Forms.{longNumber, nonEmptyText, tuple}
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)(
    implicit executionContext: ExecutionContext
) extends BaseController
    with I18nSupport {

  def index() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Home(vv))
  }

  def list(): Action[AnyContent] = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "TODO",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      todoItems      <- lib.persistence.onMySQL.TodoRepository.getAll()
      todoCategories <-
        Future.sequence(
          todoItems
            .map(todo =>
              lib.persistence.onMySQL.TodoCategoryRepository
                .get(TodoCategory.Id(todo.v.categoryId))
            )
            .map(_.map { Success(_) }.recover { case t => Failure(t) })
        )
    } yield {
      val todoWithCategory = todoItems
        .zip(todoCategories.map(x => x.toOption.flatten))
        .map(x => (x._1.v, x._2.map(_.v)))
      Ok(
        views.html.pages
          .List(vv, todoWithCategory)
      )
    }
  }

  def category(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "カテゴリー",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Category(vv))
  }

  val createForm: Form[(String, String, Long)] = Form(
    tuple(
      "title"    -> nonEmptyText,
      "body"     -> nonEmptyText,
      "category" -> longNumber
    )
  )

  val updateForm: Form[(String, String)] = Form(
    tuple(
      "title" -> nonEmptyText,
      "body"  -> nonEmptyText
    )
  )

  def create(): Action[AnyContent] = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "新規作成",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      categories <- lib.persistence.onMySQL.TodoCategoryRepository.getAll()
    } yield Ok(views.html.pages.Create(vv, createForm, categories.map(_.v)))
  }

  def postCreate(): Action[AnyContent] = Action async { implicit req =>
    createForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val vv = ViewValueHome(
            title  = "新規作成",
            cssSrc = Seq("main.css"),
            jsSrc  = Seq("main.js")
          )
          for {
            categories <-
              lib.persistence.onMySQL.TodoCategoryRepository.getAll()
          } yield BadRequest(
            views.html.pages.Create(vv, formWithErrors, categories.map(_.v))
          )
        },
        formData => {
          lib.persistence.onMySQL.TodoRepository
            .add(
              new Todo(
                id         = None,
                categoryId = formData._3,
                title      = formData._1,
                body       = formData._2,
                state      = TodoState.Todo
              ).toWithNoId
            )
            .map(
              // 追加が完了したら一覧画面へリダイレクト
              _ => Redirect(routes.HomeController.list())
            )
        }
      )
  }

  def edit(id: Long): Action[AnyContent] = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "編集",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      todoItem <- lib.persistence.onMySQL.TodoRepository.get(Todo.Id(id))
    } yield {
      todoItem match {
        case Some(v) =>
          Ok(
            views.html.pages
              .Edit(
                vv,
                v.v.toEmbeddedId.id,
                updateForm.fill((v.v.title, v.v.body))
              )
          )
        case None    => Ok("Not FOUND")
      }
    }
  }

  def todo(id: Long): Action[AnyContent] = Action async { implicit req =>
    for {
      todoItem <- lib.persistence.onMySQL.TodoRepository.get(Todo.Id(id))
    } yield {
      todoItem match {
        case Some(v) => {
          val vv = ViewValueHome(
            title  = v.v.title,
            cssSrc = Seq("main.css"),
            jsSrc  = Seq("main.js")
          )
          Ok(views.html.pages.TodoView(vv, v.v))
        }
        case None    => Ok("Not FOUND")
      }
    }
  }

  def update(id: Long): Action[AnyContent] = Action async { implicit req =>
    updateForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[(String, String)]) => {
          val vv = ViewValueHome(
            title  = "編集",
            cssSrc = Seq("main.css"),
            jsSrc  = Seq("main.js")
          )
          Future.successful(
            BadRequest(views.html.pages.Edit(vv, id, formWithErrors))
          )
        },
        (data: (String, String)) => {
          lib.persistence.onMySQL.TodoRepository
            .get(Todo.Id(id))
            .flatMap {
              case Some(x) =>
                lib.persistence.onMySQL.TodoRepository
                  .update(x.map(_.copy(title = data._1, body = data._2)))
                  .map(_ => Redirect(routes.HomeController.list()))
              case None    =>
                Future.successful(
                  NotFound("Not a such ID")
                )
            }
        }
      )
  }

  def delete(): Action[AnyContent] = Action async { implicit req =>
    req.body.asFormUrlEncoded
      .get("id")
      .headOption
      .flatMap(x => Try(x.toLong).toOption) match {
      case Some(id) =>
        for {
          result <- lib.persistence.onMySQL.TodoRepository
                      .remove(Todo.Id(id))
                      .map(_ => Redirect(routes.HomeController.list()))
        } yield result
      case None     => Future.successful(NotFound("No such a ID"))
    }
  }
}
