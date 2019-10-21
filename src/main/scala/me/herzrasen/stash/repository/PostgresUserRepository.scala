package me.herzrasen.stash.repository

import io.getquill._
import me.herzrasen.stash.domain.{Roles, User}
import monix.execution.Scheduler

import scala.concurrent.Future

class PostgresUserRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends UserRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import ctx._

  def createTable(): Unit = {
    val connection = ctx.dataSource.getConnection
    val createTable = connection.prepareStatement(User.createTableStatement)
    createTable.execute()
    ()
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

  object Encoders {

    implicit val encodeRole: MappedEncoding[Roles.Role, String] =
      MappedEncoding(
        _.mkString
      )

    implicit val decodeRole: MappedEncoding[String, Roles.Role] =
      MappedEncoding(
        Roles.parse
      )
  }
}
