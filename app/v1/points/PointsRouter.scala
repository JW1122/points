package v1.points

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the PostResource controller.
  */
class PointsRouter @Inject()(controller: PointsController) extends SimpleRouter {
  val prefix = "/v1/points"

  def link(vendor: String): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / vendor
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.balance

    case POST(p"/add") =>
      controller.processCreate

    case POST(p"/deduct") =>
      controller.processDeduct

    case GET(p"/$id") =>
      controller.show(id)
  }

}
