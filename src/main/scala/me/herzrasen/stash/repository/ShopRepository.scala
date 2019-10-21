package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.Shop

import scala.concurrent.Future

trait ShopRepository {
  def createTable(): Unit
  def create(shop: Shop): Future[Shop]
  def delete(shop: Shop): Future[Unit]
  def findAll(): Future[List[Shop]]
  def find(id: Int): Future[Option[Shop]]
}
