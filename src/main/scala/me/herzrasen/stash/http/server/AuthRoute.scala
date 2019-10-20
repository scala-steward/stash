package me.herzrasen.stash.http.server
import akka.http.scaladsl.server.directives.PathDirectives
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials._
import akka.http.scaladsl.server.Directives._
import me.herzrasen.stash.domain.User
import scala.concurrent.Future
import me.herzrasen.stash.repository.UserRepository
import scala.concurrent.ExecutionContext
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.JwtUtil

class AuthRoute()(implicit repository: UserRepository, ec: ExecutionContext)
    extends SecurityDirectives
    with PathDirectives
    with RouteDirectives
    with StrictLogging {

  val route: Route =
    path("v1" / "token") {
      get {
        authenticateBasicAsync(realm = "secure site", authenticator) { user =>
          val token = JwtUtil.create(user)
          complete(s"$token")
        }
      }
    }

  def authenticator(credentials: Credentials): Future[Option[User]] =
    credentials match {
      case p @ Provided(id) =>
        repository.find(id).map {
          case Some(user) =>
            if (p.verify(user.password, hasher = JwtUtil.hash)) {
              logger.debug(s"Authenticated user: ${user.name}")
              Some(user)
            } else None
          case None =>
            None
        }
      case _ => Future.successful(None)
    }

}