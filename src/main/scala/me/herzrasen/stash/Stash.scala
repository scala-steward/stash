package me.herzrasen.stash

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import io.getquill._
import me.herzrasen.stash.ConfigFields._
import me.herzrasen.stash.auth.HmacSecret
import me.herzrasen.stash.http.server.{Routes, WebServer}
import me.herzrasen.stash.repository._

import scala.concurrent.ExecutionContext

object Stash extends App with RouteConcatenation with StrictLogging {
  logger.info("Stash server starting...")

  private val config: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("stash")
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  implicit val userRepository: UserRepository = new PostgresUserRepository()
  userRepository.createTable()
  userRepository.initializeAdminUser().map {
    case Some(initialAdminPassword) =>
      logger.info(s"Initial admin password: >>>> $initialAdminPassword <<<<")
    case None =>
      logger.debug("Admin user already exists. No new one created.")
  }

  implicit val shopRepository: ShopRepository = new PostgresShopRepository()
  shopRepository.createTable()

  implicit val quantityRepository: QuantityRepository =
    new PostgresQuantityRepository()
  quantityRepository.createTable()

  implicit val itemRepository: ItemRepository = new PostgresItemRepository()
  itemRepository.createTable()

  implicit val hmacSecret: HmacSecret = HmacSecret(config.hmacSecret)

  val webServer: WebServer =
    WebServer.start(config.httpServerInterface, config.httpServerPort, Routes())

  sys.addShutdownHook {
    logger.info("Shutting down stash...")
    webServer.shutdown
    ()
  }

}
