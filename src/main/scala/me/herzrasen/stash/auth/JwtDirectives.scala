package me.herzrasen.stash.auth

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{AuthorizationFailedRejection, Directive1}
import me.herzrasen.stash.domain.Roles._
import me.herzrasen.stash.domain._

trait JwtDirectives extends HeaderDirectives with RouteDirectives {

  def authorizeAdmin: Directive1[Int] =
    authorize(Roles.isAdmin)

  def authorize: Directive1[Int] =
    authorize(r => !Roles.isUnknown(r))

  private def authorize(f: Role => Boolean): Directive1[Int] =
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(token) =>
        BearerToken(token).token match {
          case Some(bt) =>
            if (JwtUtil.isExpired(bt)) {
              reject(AuthorizationFailedRejection)
            } else {
              val role = JwtUtil.role(bt)
              if (f(role)) {
                JwtUtil.id(bt) match {
                  case Some(id) => provide(id)
                  case None => reject(AuthorizationFailedRejection)
                }
              } else {
                reject(AuthorizationFailedRejection)
              }
            }
          case None => reject(AuthorizationFailedRejection)
        }
      case _ =>
        reject(AuthorizationFailedRejection)
    }

}

object JwtDirectives extends JwtDirectives
