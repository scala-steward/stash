package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.{FutureDirectives, RouteDirectives}
import me.herzrasen.stash.json.JsonSupport
import spray.json.RootJsonFormat

import scala.concurrent.Future
import scala.util.{Failure, Success}

object RouteUtil
    extends RouteDirectives
    with FutureDirectives
    with JsonSupport
    with SprayJsonSupport {

  def findAllAndComplete[T](
      finder: () => Future[List[T]]
  )(implicit jsonFormat: RootJsonFormat[T]): Route =
    onComplete(finder()) {
      case Success(l) =>
        complete(l)
      case Failure(ex) =>
        complete(StatusCodes.InternalServerError -> ex)
    }

  def findAndRun[T](
      id: Int,
      finder: Int => Future[Option[T]],
      deletor: T => Future[Unit]
  ): Route =
    onComplete(finder(id)) {
      case Success(entityOpt) =>
        entityOpt match {
          case Some(entity) =>
            onComplete(deletor(entity)) {
              case Success(_) =>
                complete(StatusCodes.OK)
              case Failure(_) =>
                complete(StatusCodes.NotModified)
            }
          case None =>
            complete(StatusCodes.NotFound)
        }
      case Failure(ex) =>
        complete(StatusCodes.InternalServerError -> ex)
    }
}
