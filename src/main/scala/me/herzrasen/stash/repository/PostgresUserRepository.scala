package me.herzrasen.stash.repository

import java.util.UUID

import io.getquill._
import me.herzrasen.stash.auth.JwtUtil
import me.herzrasen.stash.domain.{Roles, User}
import monix.execution.Scheduler

import scala.concurrent.Future

class PostgresUserRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends UserRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import Encoders._
  import ctx._

  def createTable(): Unit = {
    val connection = ctx.dataSource.getConnection
    val createTable = connection.prepareStatement(User.createTableStatement)
    createTable.execute()
    ()
  }

  def initializeAdminUser(): Future[Option[String]] =
    findAll().flatMap { users =>
      if (users.isEmpty) {
        val password = UUID.randomUUID().toString
        val admin = User(0, "admin", JwtUtil.hash(password), Roles.Admin)
        create(admin).map(_ => Some(password))
      } else {
        Future.successful(None)
      }
    }

  def create(user: User): Future[User] =
    ctx
      .run {
        quote {
          querySchema[User]("stash_user")
            .insert(
              _.name -> lift(user.name),
              _.password -> lift(user.password),
              _.role -> lift(user.role)
            )
            .returning(_.id)
        }
      }
      .map(id => user.copy(id = id))
      .runToFuture

  def delete(user: User): Future[Unit] =
    ctx
      .run {
        quote {
          querySchema[User]("stash_user").filter(_.id == lift(user.id)).delete
        }
      }
      .map(_ => ())
      .runToFuture

  def findAll(): Future[List[User]] =
    ctx.run {
      quote {
        querySchema[User]("stash_user")
      }
    }.runToFuture

  def find(id: Int): Future[Option[User]] =
    ctx
      .run {
        quote {
          querySchema[User]("stash_user").filter(_.id == lift(id))
        }
      }
      .runToFuture
      .map(_.headOption)

  def find(name: String): Future[Option[User]] =
    ctx
      .run {
        quote {
          querySchema[User]("stash_user").filter(_.name == lift(name))
        }
      }
      .runToFuture
      .map(_.headOption)

  private object Encoders {

    implicit val encodeRole: MappedEncoding[Roles.Role, String] =
      MappedEncoding(
        _.mkString()
      )

    implicit val decodeRole: MappedEncoding[String, Roles.Role] =
      MappedEncoding(
        Roles.parse
      )
  }
}
