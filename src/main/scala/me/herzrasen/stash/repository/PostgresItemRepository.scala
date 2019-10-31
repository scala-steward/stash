package me.herzrasen.stash.repository

import io.getquill.{PostgresMonixJdbcContext, SnakeCase}
import me.herzrasen.stash.domain.Item
import monix.execution.Scheduler

import scala.concurrent.Future

class PostgresItemRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends ItemRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global
  import ctx._

  override def createTable(): Unit = {
    val connection = ctx.dataSource.getConnection
    val createTable = connection.prepareStatement(Item.createTableStatement)
    createTable.execute()
    ()
  }

  override def create(item: Item): Future[Item] =
    ctx
      .run {
        quote {
          query[Item]
            .insert(
              _.name -> lift(item.name),
              _.inStock -> lift(item.inStock),
              _.warnAt -> lift(item.warnAt),
              _.quantityId -> lift(item.quantityId),
              _.shopId -> lift(item.shopId)
            )
            .returning(_.id)
        }
      }
      .map(id => item.copy(id = id))
      .runToFuture

  override def findAll(): Future[List[Item]] =
    ctx.run {
      quote {
        query[Item]
      }
    }.runToFuture

  override def find(id: Int): Future[Option[Item]] =
    ctx
      .run {
        quote {
          query[Item].filter(_.id == lift(id))
        }
      }
      .map(_.headOption)
      .runToFuture

  override def delete(item: Item): Future[Unit] =
    ctx
      .run {
        quote {
          query[Item].filter(_.id == lift(item.id)).delete
        }
      }
      .map(_ => ())
      .runToFuture
}
