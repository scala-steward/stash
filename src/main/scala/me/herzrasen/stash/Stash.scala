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
import me.herzrasen.stash.http.server.AuthRoute
import akka.http.scaladsl.server.RouteConcatenation
import scala.concurrent.ExecutionContext
import me.herzrasen.stash.auth.JwtUtil

object Stash extends App with RouteConcatenation with StrictLogging {
  logger.info("Stash server starting...")

  implicit val system: ActorSystem = ActorSystem("stash")
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit val ctx: PostgresMonixJdbcContext[SnakeCase] =
    new PostgresMonixJdbcContext(SnakeCase, "postgres")

  implicit val repository: UserRepository = new PostgresUserRepository()
  repository.createTable()

  val password = JwtUtil.hash("test123")
  println(s"$password")

  val route: Route = new UserRoute().route ~ new AuthRoute().route

  Http().bindAndHandle(route, "0.0.0.0", 8080)
}
