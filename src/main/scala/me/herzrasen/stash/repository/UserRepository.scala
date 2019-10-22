package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.User

import scala.concurrent.Future

trait UserRepository {
  def createTable(): Unit
  def initializeAdminUser(): Future[Option[String]]
  def create(user: User): Future[User]
  def delete(user: User): Future[Unit]
  def findAll(): Future[List[User]]
  def find(id: Int): Future[Option[User]]
  def find(name: String): Future[Option[User]]
}
