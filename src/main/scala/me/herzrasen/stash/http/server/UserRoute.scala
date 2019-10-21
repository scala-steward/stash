package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{JwtDirectives, JwtUtil}
import me.herzrasen.stash.domain.{NewUser, Roles, User}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.UserRepository

import scala.util.{Failure, Success}

class UserRoute()(implicit repository: UserRepository)
    extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives
    with RouteConcatenation
    with JsonSupport
    with StrictLogging {

  val route: Route =
    path("v1" / "users") {
      authorizeAdmin {
        get {
          onComplete(repository.findAll()) {
            case Success(users) =>
              complete(users)
            case Failure(ex) =>
              complete(StatusCodes.InternalServerError -> ex)
          }
        } ~
          post {
            entity(as[NewUser]) { newUser =>
              onComplete(repository.find(newUser.name)) {
                case Success(existingUserOpt) =>
                  existingUserOpt match {
                    case None =>
                      logger.info(s"Trying to create user: ${newUser.name}")
                      onComplete(
                        repository.create(
                          User(
                            0,
                            newUser.name,
                            JwtUtil.hash(newUser.password),
                            Roles.User
                          )
                        )
                      ) {
                        case Success(user) =>
                          complete(user)
                        case Failure(ex) =>
                          complete(StatusCodes.InternalServerError -> ex)
                      }
                    case Some(_) =>
                      complete(
                        StatusCodes.Conflict -> s"User ${newUser.name} already exists"
                      )
                  }
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError -> ex)
              }
            }
          }
      }
    }
}
