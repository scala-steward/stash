package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives._
import akka.http.scaladsl.server.{
  AuthorizationFailedRejection,
  Route,
  RouteConcatenation
}
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives, JwtUtil}
import me.herzrasen.stash.domain.{NewUser, Roles, User}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.UserRepository

import scala.util.{Failure, Success}

class UserRoute()(implicit repository: UserRepository, hmacSecret: HmacSecret)
    extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives
    with RouteConcatenation
    with JsonSupport
    with StrictLogging {

  val route: Route =
    pathPrefix("v1" / "users") {
      path(IntNumber) { id =>
        authorize.apply { idFromToken =>
          getUser(id, idFromToken) ~ putPassword(id, idFromToken)
        } ~ authorizeAdmin.apply { _ =>
          deleteUser(id)
        }
      } ~
        pathEnd {
          authorizeAdmin.apply { _ =>
            getUsers ~
              postUser
          }
        }
    }

  private def getUser(id: Int, idFromToken: Int): Route =
    get {
      val f = repository.find(id).zip(repository.find(idFromToken))
      onComplete(f) {
        case Success((userFromIdOpt, userFromTokenOpt)) =>
          (userFromIdOpt, userFromTokenOpt) match {
            case (Some(userFromId), Some(userFromToken))
                if userFromId.id == userFromToken.id =>
              complete(userFromId)
            case (Some(userFromId), Some(userFromToken))
                if Roles.isAdmin(userFromToken.role) =>
              complete(userFromId)
            case (_, Some(userFromToken))
                if !Roles.isAdmin(userFromToken.role) =>
              complete(StatusCodes.Forbidden)
            case (_, _) =>
              complete(StatusCodes.NotFound)
          }
        case Failure(ex) =>
          complete(StatusCodes.InternalServerError -> ex)
      }
    }

  private def deleteUser(id: Int): Route =
    delete {
      RouteUtil.findAndRun(id, repository.find, repository.delete)
    }

  private def putPassword(id: Int, idFromToken: Int): Route =
    put {
      entity(as[String]) { newPassword =>
        if (id == idFromToken) {
          RouteUtil.findAndRun(
            id,
            repository.find,
            user => repository.updatePassword(user, JwtUtil.hash(newPassword))
          )
        } else {
          reject(AuthorizationFailedRejection)
        }
      }
    }

  private def getUsers: Route =
    get {
      RouteUtil.findAllAndComplete(repository.findAll)
    }

  private def postUser: Route =
    post {
      entity(as[NewUser]) { newUser =>
        onComplete(repository.find(newUser.name)) {
          case Success(existingUserOpt) =>
            existingUserOpt match {
              case None =>
                logger.info(s"Trying to create user: ${newUser.name}")
                RouteUtil.createAndComplete(
                  User(
                    0,
                    newUser.name,
                    JwtUtil.hash(newUser.password),
                    Roles.User
                  ),
                  repository.create
                )
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
