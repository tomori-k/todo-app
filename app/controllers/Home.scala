/** to do sample project
  */

package controllers

import lib.model.{Todo, TodoState}
import model.ViewValueHome
import play.api.data.Form
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

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
      todoItems <- lib.persistence.onMySQL.TodoRepository.getAll()
    } yield {
      Ok(views.html.pages.List(vv, todoItems.map(x => x.v)))
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

  val createForm: Form[(String, String)] = Form(
    tuple(
      "title" -> nonEmptyText,
      "body"  -> nonEmptyText
    )
  )

  val updateForm: Form[(String, String)] = Form(
    tuple(
      "title" -> nonEmptyText,
      "body"  -> nonEmptyText
    )
  )

  def create(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "新規作成",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Create(vv, createForm))
  }

  def postCreate(): Action[AnyContent] = Action async { implicit req =>
    createForm
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[(String, String)]) => {
          val vv = ViewValueHome(
            title  = "新規作成",
            cssSrc = Seq("main.css"),
            jsSrc  = Seq("main.js")
          )
          Future.successful(
            BadRequest(views.html.pages.Create(vv, formWithErrors))
          )
        },
        (formData: (String, String)) => {
          lib.persistence.onMySQL.TodoRepository
            .add(
              new Todo(
                id         = None,
                categoryId = 0,
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
}
