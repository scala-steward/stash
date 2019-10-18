package me.herzrasen.stash
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import me.herzrasen.stash.domain.User
import me.herzrasen.stash.domain.Roles.{User => UserRole}
import me.herzrasen.stash.domain.Shop
import io.getquill.PostgresMonixJdbcContext
import io.getquill.SnakeCase
import me.herzrasen.stash.repository.PostgresShopRepository
import me.herzrasen.stash.repository.ShopRepository
import io.getquill.SqlMirrorContext

object Stash extends App with StrictLogging {
  logger.info("Stash server starting...")

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  val repository: ShopRepository = new PostgresShopRepository()

  val shop = Await.result(repository.create(Shop(1, "Rossmann")), Duration.Inf)
  logger.info(s"Created shop: $shop")
}
