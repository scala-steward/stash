package me.herzrasen.stash.http.server

import me.herzrasen.stash.repository.UserRepository
import akka.http.scaladsl.server.Route

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import akka.http.scaladsl.server.Directives._
import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes

import me.herzrasen.stash.json.UserProtocol._
import akka.http.scaladsl.server.directives._
import me.herzrasen.stash.auth.JwtDirectives

class UserRoute()(implicit repository: UserRepository)
    extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives {

  val route: Route =
    path("v1" / "user") {
      authorizeAdmin {
        get {
          onComplete(repository.findAll()) {
            case Success(users) =>
              complete(users)
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError -> ex)
          }
        }
      }
    }
}
