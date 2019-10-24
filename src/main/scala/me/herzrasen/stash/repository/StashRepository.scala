package me.herzrasen.stash.repository

import scala.concurrent.Future

trait StashRepository[T, I] {

  def create(t: T): Future[T]
  def findAll(): Future[List[T]]
  def find(id: I): Future[Option[T]]
  def delete(t: T): Future[Unit]

}
