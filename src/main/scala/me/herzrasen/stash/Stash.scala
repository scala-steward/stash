package me.herzrasen.stash
import com.typesafe.scalalogging.StrictLogging

import io.getquill.PostgresMonixJdbcContext
import io.getquill.SnakeCase
import akka.http.scaladsl.Http
import me.herzrasen.stash.http.server.UserRoute
import akka.actor.ActorSystem
import me.herzrasen.stash.repository.UserRepository
import me.herzrasen.stash.repository.PostgresUserRepository
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

object Stash extends App with StrictLogging {
  logger.info("Stash server starting...")

  implicit val system: ActorSystem = ActorSystem("stash")
  implicit val am: ActorMaterializer = ActorMaterializer()

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  implicit val repository: UserRepository = new PostgresUserRepository()

  val userRoute: Route = new UserRoute().route

  Http().bindAndHandle(userRoute, "0.0.0.0", 8080)
}
