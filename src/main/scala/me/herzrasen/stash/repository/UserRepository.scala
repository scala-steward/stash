package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.User

import scala.concurrent.Future

trait UserRepository extends StashRepository[User, Int] with CreateTable {

  def initializeAdminUser(): Future[Option[String]]
  def find(name: String): Future[Option[User]]
  def updatePassword(user: User, newPassword: String): Future[Unit]

}
