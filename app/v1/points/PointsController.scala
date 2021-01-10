package v1.points

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import java.time.LocalDateTime
import scala.concurrent.{ExecutionContext, Future}

case class PointsFormInput(points: Int, vendor: Option[String], timestamp: Option[LocalDateTime])


/**
  * Takes HTTP requests and produces JSON.
  */
class PointsController @Inject()(cc: PointsControllerComponents)(
  implicit ec: ExecutionContext)
  extends PointsBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PointsFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "points" -> number,
        "vendor" -> optional(text),
        "timestamp" -> optional(localDateTime)
      )(PointsFormInput.apply)(PointsFormInput.unapply)
    )
  }

  def balance: Action[AnyContent] = PointsAction.async { implicit request =>
    logger.info("balance: ")
    pointsService.balance.map { points =>
      Ok(Json.toJson(points))
    }
  }

  def processCreate: Action[AnyContent] = PointsAction.async { implicit request =>
    logger.info("processCreate: ")
    processJsonPointsCreate()
  }

  def processDeduct: Action[AnyContent] = PointsAction.async { implicit request =>
    logger.info("processDeduct: ")
    processJsonPointsDeduct()
  }

  def show(id: String): Action[AnyContent] = PointsAction.async {
    implicit request =>
      logger.info(s"show: id = $id")
      pointsService.lookup(id).map { points =>
        Ok(Json.toJson(points))
      }
  }

  private def processJsonPointsCreate[A]()(
    implicit request: PointsRequest[A]): Future[Result] = {
    def failure(badForm: Form[PointsFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PointsFormInput) = {
      pointsService.create(input).map { points =>
        Created(Json.toJson(points))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  private def processJsonPointsDeduct[A]()(
    implicit request: PointsRequest[A]): Future[Result] = {
    def failure(badForm: Form[PointsFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PointsFormInput) = {
      pointsService.deduct(input).map { points =>
        Created(Json.toJson(points))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
