package me.herzrasen.stash.repository
import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.domain.Quantity
import monix.execution.Scheduler

import scala.concurrent.Future

class PostgresQuantityRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends QuantityRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import ctx._

  override def createTable(): Unit = {
    val connection = ctx.dataSource.getConnection
    val createTable = connection.prepareStatement(Quantity.createTableStatement)
    createTable.execute()
    ()
  }

  override def create(quantity: Quantity): Future[Quantity] =
    ctx
      .run {
        quote {
          query[Quantity]
            .insert(
              _.name -> lift(quantity.name),
              _.abbreviation -> lift(quantity.abbreviation)
            )
            .returning(_.id)
        }
      }
      .map(id => quantity.copy(id = id))
      .runToFuture

  override def findAll(): Future[List[Quantity]] =
    ctx.run {
      quote {
        query[Quantity]
      }
    }.runToFuture

  override def find(id: Int): Future[Option[Quantity]] =
    ctx
      .run {
        quote {
          query[Quantity].filter(_.id == lift(id))
        }
      }
      .runToFuture
      .map(_.headOption)

  override def delete(quantity: Quantity): Future[Unit] =
    ctx
      .run {
        quote {
          query[Quantity].filter(_.id == lift(quantity.id)).delete
        }
      }
      .runToFuture
      .map(_ => ())

}
