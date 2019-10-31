package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.Item

import scala.collection.mutable
import scala.concurrent.Future

class InMemoryItemRepository extends ItemRepository {

  private val db: mutable.ListBuffer[Item] = mutable.ListBuffer.empty

  override def createTable(): Unit = ()

  override def create(item: Item): Future[Item] = {
    db += item
    Future.successful(item)
  }

  override def delete(item: Item): Future[Unit] = {
    db -= item
    Future.successful(())
  }

  override def findAll(): Future[List[Item]] =
    Future.successful(db.toList)

  override def find(id: Int): Future[Option[Item]] =
    Future.successful(db.find(_.id == id))
}
