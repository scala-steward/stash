package me.herzrasen.stash.http.server
import akka.http.scaladsl.server.directives.PathDirectives
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.http.scaladsl.server.directives.Credentials._
import java.util.Base64
import akka.http.scaladsl.server.Directives._
import me.herzrasen.stash.domain.User
import scala.concurrent.Future
import me.herzrasen.stash.repository.UserRepository
import scala.concurrent.ExecutionContext

class AuthRoute()(implicit repository: UserRepository, ec: ExecutionContext)
    extends SecurityDirectives
    with PathDirectives
    with RouteDirectives {

  val route: Route =
    path("v1" / "token") {
      get {
        authenticateBasicAsync(realm = "secure site", authenticator) { id =>
          complete(s"$id")
        }
      }
    }

  def authenticator(credentials: Credentials): Future[Option[User]] =
    credentials match {
      case p @ Provided(id) =>
        repository.find(id).map {
          case Some(user) =>
            if (p.verify(user.password, hasher = hash)) Some(user)
            else None
          case None =>
            None
        }
      case _ => Future.successful(None)
    }

  def hash(password: String): String = {
    import java.security.MessageDigest
    val bytes =
      MessageDigest.getInstance("SHA-256").digest(password.getBytes("UTF-8"))
    Base64.getEncoder.encodeToString(bytes)
  }
}
