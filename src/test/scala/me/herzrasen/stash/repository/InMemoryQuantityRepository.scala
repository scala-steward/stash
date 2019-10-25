package me.herzrasen.stash.repository
import me.herzrasen.stash.domain.Quantity

import scala.collection.mutable
import scala.concurrent.Future

class InMemoryQuantityRepository extends QuantityRepository {

  private val db: mutable.ListBuffer[Quantity] = mutable.ListBuffer.empty

  override def createTable(): Unit = ()

  override def create(quantity: Quantity): Future[Quantity] =
    db.find(_.name == quantity.name) match {
      case Some(_) =>
        Future.failed(new IllegalArgumentException(s"${quantity.name} exists"))
      case None =>
        db += quantity
        Future.successful(quantity)
    }

  override def findAll(): Future[List[Quantity]] =
    Future.successful(db.toList)

  override def find(id: Int): Future[Option[Quantity]] =
    Future.successful {
      db.find(_.id == id)
    }

  override def delete(quantity: Quantity): Future[Unit] = {
    db -= quantity
    Future.successful(())
  }

}
