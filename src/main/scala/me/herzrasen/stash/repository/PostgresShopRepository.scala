package me.herzrasen.stash.repository

import io.getquill._
import me.herzrasen.stash.domain.Shop
import monix.execution.Scheduler

import scala.concurrent.Future

class PostgresShopRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends ShopRepository {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import ctx._

  def createTable(): Unit = {
    val connection = ctx.dataSource.getConnection
    val createTable = connection.prepareStatement(Shop.createTableStatement)
    createTable.execute()
    ()
  }

  def create(shop: Shop): Future[Shop] =
    ctx
      .run {
        quote {
          query[Shop].insert { _.name -> lift(shop.name) }.returning(_.id)
        }
      }
      .map(id => shop.copy(id = id))
      .runToFuture

  def delete(shop: Shop): Future[Unit] =
    ctx
      .run {
        quote {
          query[Shop].filter(_.id == lift(shop.id)).delete
        }
      }
      .map(_ => ())
      .runToFuture

  def findAll(): Future[List[Shop]] =
    ctx.run {
      quote {
        query[Shop]
      }
    }.runToFuture

  def find(id: Int): Future[Option[Shop]] =
    ctx
      .run {
        quote {
          query[Shop].filter(_.id == lift(id))
        }
      }
      .runToFuture
      .map(_.headOption)

}
