package me.herzrasen.stash.http.server

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.RouteConcatenation
import akka.http.scaladsl.server.directives._
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.ShopRepository

import scala.util.{Failure, Success}

class ShopRoute()(implicit repository: ShopRepository, hmacSecret: HmacSecret)
    extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives
    with RouteConcatenation
    with MethodDirectives
    with JsonSupport
    with StrictLogging {

  val route: Route =
    pathPrefix("v1" / "shops") {
      pathEnd {
        get {
          authorize.apply { _ =>
            onComplete(repository.findAll()) {
              case Success(shops) =>
                complete(shops)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError -> ex)
            }
          }
        }
      }
    }

}
