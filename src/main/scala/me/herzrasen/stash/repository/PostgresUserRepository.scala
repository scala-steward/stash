package me.herzrasen.stash.repository
import io.getquill.PostgresMonixJdbcContext
import io.getquill.SnakeCase
import scala.concurrent.Future
import monix.execution.Scheduler
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.Role
import me.herzrasen.stash.domain.Roles

class PostgresUserRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends UserRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import ctx._
  import Encoders._

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

  object Encoders {
    import io.getquill.MappedEncoding

    implicit val encodeRole: MappedEncoding[Role, String] = MappedEncoding(
      _.mkString
    )

    implicit val decodeRole: MappedEncoding[String, Role] = MappedEncoding(
      Roles.parse
    )
  }
}
