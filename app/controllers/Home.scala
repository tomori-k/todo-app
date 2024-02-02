/** to do sample project
  */

package controllers

import model.ViewValueHome
import play.api.mvc._

import javax.inject._

@Singleton
class HomeController @Inject() (val controllerComponents: ControllerComponents)
    extends BaseController {

  def index() = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "Home",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Home(vv))
  }

  def list(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "TODO",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.List(vv))
  }

  def category(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "カテゴリー",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Category(vv))
  }

  def create(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "新規作成",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Create(vv))
  }

  def edit(): Action[AnyContent] = Action { implicit req =>
    val vv = ViewValueHome(
      title  = "編集",
      cssSrc = Seq("main.css"),
      jsSrc  = Seq("main.js")
    )
    Ok(views.html.pages.Edit(vv))
  }
}
