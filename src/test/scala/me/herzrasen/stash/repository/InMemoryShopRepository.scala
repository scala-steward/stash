package me.herzrasen.stash.repository
import me.herzrasen.stash.domain.Shop

import scala.collection.mutable
import scala.concurrent.Future

class InMemoryShopRepository extends ShopRepository {

  private val db: mutable.ListBuffer[Shop] = mutable.ListBuffer.empty

  override def createTable(): Unit = ()

  override def create(shop: Shop): Future[Shop] = {
    db += shop
    Future.successful(shop)
  }

  override def delete(shop: Shop): Future[Unit] = {
    db -= shop
    Future.successful(())
  }

  override def findAll(): Future[List[Shop]] =
    Future.successful(db.toList)

  override def find(id: Int): Future[Option[Shop]] =
    Future.successful(db.find(_.id == id))
}
