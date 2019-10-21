package me.herzrasen.stash

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import io.getquill._
import me.herzrasen.stash.http.server.AuthRoute
import me.herzrasen.stash.http.server.UserRoute
import me.herzrasen.stash.repository.PostgresUserRepository
import me.herzrasen.stash.repository.UserRepository
import scala.concurrent.ExecutionContext

object Stash extends App with RouteConcatenation with StrictLogging {
  logger.info("Stash server starting...")

  implicit val system: ActorSystem = ActorSystem("stash")
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  implicit val repository: UserRepository = new PostgresUserRepository()
  repository.createTable()

  val route: Route = new UserRoute().route ~ new AuthRoute().route

  val port: Int = 8080

  Http().bindAndHandle(route, "0.0.0.0", port)
}
