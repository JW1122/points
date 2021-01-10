package v1.points

import play.api.MarkerContext
import play.api.libs.json._

import java.time.LocalDateTime
import javax.inject.Inject
import scala.collection.Map
import scala.concurrent.{ExecutionContext, Future}

case class PointsResource(
  vendor: String,
  points: Int,
  timestamp: LocalDateTime)

object PointsResource {
  implicit val format: Format[PointsResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[PointsResource]]
  */
class PointsService @Inject()(
  pointsRepository: PointsRepository)(implicit ec: ExecutionContext) {

  def create(pointsInput: PointsFormInput)(
    implicit mc: MarkerContext): Future[Map[String, Int]] = {
    val t = pointsInput.timestamp.getOrElse(LocalDateTime.now)
    val data = PointsData(pointsInput.points, pointsInput.vendor.getOrElse(""), t)
    pointsRepository.create(data)
  }

  def deduct(pointsInput: PointsFormInput)(
    implicit mc: MarkerContext): Future[Map[String, Int]] = {
    val t = pointsInput.timestamp.getOrElse(LocalDateTime.now)
    val data = PointsData(pointsInput.points, pointsInput.vendor.getOrElse(""), t)
    pointsRepository.deduct(data)
  }

  def lookup(id: String)(
    implicit mc: MarkerContext): Future[PointsResource] = {
    val pointsFuture = pointsRepository.get(id)
    pointsFuture.map { pointsData =>
       createPointsResource(pointsData)
    }
  }

  def balance(implicit mc: MarkerContext): Future[Map[String, Int]] = pointsRepository.listPoints

  private def createPointsResource(p: PointsData): PointsResource = {
    PointsResource(
      p.vendor,
      p.points,
      p.timestamp)
  }

}
