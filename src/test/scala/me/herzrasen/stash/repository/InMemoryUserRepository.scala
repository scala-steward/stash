package me.herzrasen.stash.repository

import me.herzrasen.stash.domain.User

import scala.collection.mutable
import scala.concurrent.Future

class InMemoryUserRepository extends UserRepository {

  private val db: mutable.ListBuffer[User] = mutable.ListBuffer.empty

  def createTable(): Unit =
    ()

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

}
