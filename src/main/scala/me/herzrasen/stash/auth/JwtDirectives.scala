package me.herzrasen.stash.auth
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directive0
import me.herzrasen.stash.domain._
import me.herzrasen.stash.domain.Roles._
import akka.http.scaladsl.server.AuthorizationFailedRejection
import me.herzrasen.stash.auth.BearerToken

trait JwtDirectives extends HeaderDirectives with RouteDirectives {

  def authorizeAdmin: Directive0 =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(token) =>
        BearerToken(token).token match {
          case Some(bt) =>
            if (JwtUtil.isExpired(bt)) {
              reject(AuthorizationFailedRejection)
            } else {
              val role = roleFromToken(bt)
              if (Roles.isAdmin(role))
                pass
              else
                reject(AuthorizationFailedRejection)
            }
          case None => reject(AuthorizationFailedRejection)
        }
      case _ =>
        reject(AuthorizationFailedRejection)
    }

  def authorize: Directive0 =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(token) =>
        BearerToken(token).token match {
          case Some(bt) =>
            if (JwtUtil.isExpired(bt)) {
              reject(AuthorizationFailedRejection)
            } else {
              val role = roleFromToken(bt)
              if (Roles.isUnknown(role))
                reject(AuthorizationFailedRejection)
              else
                pass
            }
          case None => reject(AuthorizationFailedRejection)
        }
      case _ =>
        reject(AuthorizationFailedRejection)
    }

  private def roleFromToken(jwt: String): Role =
    JwtUtil.role(jwt) match {
      case Some(role) => Roles.parse(role)
      case _ => Roles.Unknown
    }
}

object JwtDirectives extends JwtDirectives
