package me.herzrasen.stash.repository

import scala.concurrent.Future

import me.herzrasen.stash.domain.Shop

trait ShopRepository {
  def create(shop: Shop): Future[Shop]
  def delete(shop: Shop): Future[Unit]
  def findAll(): Future[List[Shop]]
  def find(id: Int): Future[Option[Shop]]
}
