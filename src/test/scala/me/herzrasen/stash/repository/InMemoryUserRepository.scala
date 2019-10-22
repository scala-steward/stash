package me.herzrasen.stash.repository

import me.herzrasen.stash.auth.JwtUtil
import me.herzrasen.stash.domain.{Roles, User}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InMemoryUserRepository extends UserRepository {

  private val db: mutable.ListBuffer[User] = mutable.ListBuffer.empty

  def createTable(): Unit =
    ()

  def initializeAdminUser(): Future[Option[String]] =
    if (db.isEmpty) {
      val password = "test123"
      val admin = User(1, "admin", JwtUtil.hash(password), Roles.Admin)
      create(admin).map(_ => Some(password))
    } else {
      Future.successful(None)
    }

  def create(user: User): Future[User] = {
    db.addOne(user)
    Future.successful(user)
  }

  def delete(user: User): Future[Unit] = {
    db -= user
    Future.successful(())
  }

  def findAll(): Future[List[User]] =
    Future.successful(db.toList)

  def find(id: Int): Future[Option[User]] =
    Future.successful(db.find(_.id == id))

  def find(name: String): Future[Option[User]] =
    Future.successful(db.find(_.name == name))

  def updatePassword(user: User, newPassword: String): Future[Unit] =
    Future.successful {
      db -= user
      db.addOne(user.copy(password = newPassword))
    }

}
