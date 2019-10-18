package me.herzrasen.stash.repository
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.domain.Shop
import scala.concurrent.Future

import scala.concurrent.ExecutionContext
import io.getquill.PostgresJdbcContext
import io.getquill.SnakeCase
import io.getquill.PostgresMonixJdbcContext
import monix.eval.Task
import monix.execution.Scheduler
import monix.execution.CancelableFuture

class PostgresShopRepository()(
    implicit ctx: PostgresMonixJdbcContext[SnakeCase]
) extends ShopRepository
    with StrictLogging {

  implicit val scheduler: Scheduler = monix.execution.Scheduler.global

  import ctx._

  def create(shop: Shop): Future[Shop] = {
    ctx
      .run {
        quote {
          query[Shop].insert { _.name -> lift(shop.name) }.returning(_.id)
        }
      }
      .map(id => shop.copy(id = id))
      .runToFuture
  }

  def delete(shop: Shop): Future[Unit] = ???

  def findAll(): Future[List[Shop]] = ???

  def find(id: Int): Future[Shop] = ???

}
