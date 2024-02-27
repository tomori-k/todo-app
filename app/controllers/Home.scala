/** to do sample project
  */

package controllers

import lib.model.Todo.TodoState
import lib.model.{Todo, TodoCategory}
import lib.persistence.default._
import model.ViewValueHome
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText, shortNumber}
import play.api.i18n.I18nSupport
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class CreateFormData(
    title:      String,
    body:       String,
    categoryId: Long
)

case class UpdateFormData(
    title:      String,
    body:       String,
    categoryId: Long,
    stateValue: Short
)

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
      todoItems <- TodoRepository.getAllWithCategory()
    } yield Ok(
      views.html.pages
        .List(vv, todoItems.map(_.v))
    )
  }

  def category(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "カテゴリー",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Category(vv))
  }

  private val createForm: Form[CreateFormData] = Form(
    mapping(
      "title"    -> nonEmptyText,
      "body"     -> nonEmptyText,
      "category" -> longNumber
    )(CreateFormData.apply)(CreateFormData.unapply)
  )

  private val updateForm: Form[UpdateFormData] = Form(
    mapping(
      "title"    -> nonEmptyText,
      "body"     -> nonEmptyText,
      "category" -> longNumber,
      "state"    -> shortNumber
    )(UpdateFormData.apply)(UpdateFormData.unapply)
  )

  def create(): Action[AnyContent] = Action async { implicit req =>
    val vv = ViewValueHome(
      title  = "新規作成",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    for {
      categories <- TodoCategoryRepository.getAll()
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
              TodoCategoryRepository.getAll()
          } yield BadRequest(
            views.html.pages.Create(vv, formWithErrors, categories.map(_.v))
          )
        },
        formData => {
          TodoRepository
            .add(
              new Todo(
                id         = None,
                categoryId = TodoCategory.Id(formData.categoryId),
                title      = formData.title,
                body       = formData.body,
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
      todoItem   <- TodoRepository.get(Todo.Id(id))
      categories <- todoItem match {
                      case Some(_) => TodoCategoryRepository.getAll()
                      case None    => Future.successful(Seq.empty)
                    }
    } yield {
      todoItem match {
        case Some(todoEntity) =>
          Ok(
            views.html.pages
              .Edit(
                vv,
                todoEntity.id,
                updateForm.fill(
                  UpdateFormData(
                    title      = todoEntity.v.title,
                    body       = todoEntity.v.body,
                    categoryId = todoEntity.v.categoryId,
                    stateValue = todoEntity.v.state.code
                  )
                ),
                categories.map(_.v)
              )
          )

        case None => NotFound("No such a todo")
      }
    }
  }

  def todo(id: Long): Action[AnyContent] = Action async { implicit req =>
    for {
      todoItem <- TodoRepository.get(Todo.Id(id))
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
        case None    => NotFound("No such a todo")
      }
    }
  }

  def update(id: Long): Action[AnyContent] = Action async { implicit req =>
    updateForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          val vv = ViewValueHome(
            title  = "編集",
            cssSrc = Seq("main.css"),
            jsSrc  = Seq("main.js")
          )
          for {
            categories <-
              TodoCategoryRepository.getAll()
          } yield BadRequest(
            views.html.pages.Edit(vv, id, formWithErrors, categories.map(_.v))
          )
        },
        data => {
          for {
            todo   <- TodoRepository.get(Todo.Id(id))
            result <- todo match {
                        case Some(x) =>
                          TodoRepository
                            .update(
                              x.map(
                                _.copy(
                                  title      = data.title,
                                  body       = data.body,
                                  categoryId = TodoCategory.Id(data.categoryId),
                                  state      = TodoState(data.stateValue)
                                )
                              )
                            )
                            .map(_ => Redirect(routes.HomeController.list()))
                        case None    =>
                          Future.successful(
                            NotFound("Not a such ID")
                          )
                      }
          } yield result
        }
      )
  }

  def delete(): Action[AnyContent] = Action async { implicit req =>
    req.body.asFormUrlEncoded
      .get("id")
      .headOption
      .flatMap(x => Try(x.toLong).toOption) match {
      case Some(id) =>
        TodoRepository
          .remove(Todo.Id(id))
          .map(_ => Redirect(routes.HomeController.list()))
      case None     => Future.successful(NotFound("No such a ID"))
    }
  }
}
