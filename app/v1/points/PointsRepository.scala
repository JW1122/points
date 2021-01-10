package v1.points

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.collection._
import scala.concurrent.Future

case class PointsData(var points: Int, vendor: String, timestamp: LocalDateTime) {
}

class PointsExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PointsRepository.
  */
trait PointsRepository {
  def create(data: PointsData)(implicit mc: MarkerContext): Future[Map[String, Int]]

  def deduct(data: PointsData)(implicit mc: MarkerContext): Future[Map[String, Int]]

  def listPoints()(implicit mc: MarkerContext): Future[Map[String, Int]]

  def get(vendor: String)(implicit mc: MarkerContext): Future[PointsData]
}

/**
  * A trivial implementation for the Points Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class PointsRepositoryImpl @Inject()()(implicit ec: PointsExecutionContext) extends PointsRepository {

  private val logger = Logger(this.getClass)
  implicit val pointsDataOrdering: Ordering[PointsData] = Ordering.by(_.timestamp)

  private val pointsList: mutable.ListBuffer[PointsData] = mutable.ListBuffer.empty[PointsData]
  create(PointsData(300, "Dannon", LocalDateTime.of(2020, 10, 31, 10, 0, 0, 0)))
  create(PointsData(200, "Unilever", LocalDateTime.of(2020, 10, 31, 11, 0, 0, 0)))
  create(PointsData(10000, "MillerCoors", LocalDateTime.of(2020, 11, 1, 14, 0, 0, 0)))
  create(PointsData(1000, "Dannon", LocalDateTime.of(2020, 11, 2, 14, 0, 0, 0)))
  private val vendorOldestPointsDateMap = mutable.Map.empty[String, LocalDateTime]

  override def listPoints()(
    implicit mc: MarkerContext): Future[Map[String, Int]] = {
    Future {
      logger.info(s"list: ")
      listByVendor
    }
  }

  private def listByVendor =
    pointsList.groupBy(_.vendor).view.mapValues(_.map(_.points).sum).toMap

  private def listDeductionsByVendor(data: Seq[PointsData]) =
    data.groupBy(_.vendor).view.mapValues(_.map(_.points).sum).toMap


  private def updateOldestDateByVendor() = {
    for (p <- pointsList) {
      val dateMapEntry = vendorOldestPointsDateMap.get(p.vendor)
      if (dateMapEntry == None) {
        vendorOldestPointsDateMap += (p.vendor -> p.timestamp)
      } else {
        if (p.timestamp.isBefore(dateMapEntry.get) && p.points > 0) {
          vendorOldestPointsDateMap.update(p.vendor, p.timestamp)
        }
      }
    }
  }


  private def deductPointsFromList(data: PointsData) = {
    var pointsToDeduct = data.points
    var pointsDeductedSeq: Seq[PointsData] = Seq.empty[PointsData]
    pointsList.sorted.zipWithIndex.foreach { case (p, index) =>
      if (pointsToDeduct > 0) {
        val vendorOldestDate = vendorOldestPointsDateMap.get(p.vendor).get
        if ((p.timestamp.isBefore(vendorOldestDate) || p.timestamp.isEqual(vendorOldestDate)) && p.points > 0) {
          if (p.points < pointsToDeduct) { //points available but not enough, deduct and move on
            pointsToDeduct -= p.points
            val pcopy = new PointsData(0, p.vendor, p.timestamp)
            pointsList.update(index, pcopy)
            val pointsDed = new PointsData(-p.points, p.vendor, LocalDateTime.now)
            pointsDeductedSeq = pointsDeductedSeq :+ pointsDed
          } else if (p.points >= pointsToDeduct) { //adequate points available, deduct and break out
            val pcopy = new PointsData(p.points - pointsToDeduct, p.vendor, p.timestamp)
            pointsList.update(index, pcopy)
            val pointsDed = new PointsData(-pointsToDeduct, p.vendor, LocalDateTime.now)
            pointsDeductedSeq = pointsDeductedSeq :+ pointsDed
            pointsToDeduct = 0
          }
        }
      }
    }
    pointsDeductedSeq
  }

  override def get(id: String)(
    implicit mc: MarkerContext): Future[PointsData] = {
    Future {
      logger.info(s"get: id = $id")
      val sum = pointsList.filter(_.vendor == id).map(_.points).sum
      val p = pointsList.find(p => p.vendor == id).getOrElse(PointsData(0, id, LocalDateTime.now))
      p.points = sum
      p
    }
  }

  def create(data: PointsData)(implicit mc: MarkerContext): Future[Map[String, Int]] = {
    Future {
      logger.info(s"create: data = $data")
      pointsList.append(data)
      logger.info(s"create: pointsList = $pointsList")
      updateOldestDateByVendor
      listByVendor
    }
  }

  def deduct(data: PointsData)(implicit mc: MarkerContext): Future[Map[String, Int]] = {
    Future {
      logger.info(s"deduct: data = $data")
      val pointsDeducted = deductPointsFromList(data)
      updateOldestDateByVendor
      listDeductionsByVendor(pointsDeducted)
    }
  }

}
