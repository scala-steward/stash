package me.herzrasen.stash

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import io.getquill._
import me.herzrasen.stash.ConfigFields._
import me.herzrasen.stash.http.server.{AuthRoute, UserRoute}
import me.herzrasen.stash.repository.{PostgresUserRepository, UserRepository}

import scala.concurrent.{ExecutionContext, Future}

object Stash extends App with RouteConcatenation with StrictLogging {
  logger.info("Stash server starting...")

  private val config: Config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("stash")
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  implicit val repository: UserRepository = new PostgresUserRepository()
  repository.createTable()

  val serverBinding =
    startWebServer(config.httpServerInterface, config.httpServerPort)

  sys.addShutdownHook {
    logger.info("Shutting down stash...")
    serverBinding.map(_.terminate(config.httpServerShutdownDeadline))
    ()
  }

  def startWebServer(interface: String, port: Int)(
      implicit system: ActorSystem, am: ActorMaterializer
  ): Future[ServerBinding] = {
    val route: Route = new UserRoute().route ~ new AuthRoute().route
    Http().bindAndHandle(route, interface, port)
  }

}
