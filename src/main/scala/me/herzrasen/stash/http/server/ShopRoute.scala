package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.http.scaladsl.server.directives._
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives}
import me.herzrasen.stash.domain.Shop
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.ShopRepository

import scala.util.{Failure, Success}

class ShopRoute()(implicit repository: ShopRepository, hmacSecret: HmacSecret)
    extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives
    with RouteConcatenation
    with MarshallingDirectives
    with MethodDirectives
    with JsonSupport
    with StrictLogging {

  val route: Route =
    pathPrefix("v1" / "shops") {
      pathEnd {
        authorize.apply { _ =>
          get {
            onComplete(repository.findAll()) {
              case Success(shops) =>
                complete(shops)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError -> ex)
            }
          } ~ post {
            entity(as[String]) { shopName =>
              RouteUtil.createAndComplete(Shop(0, shopName), repository.create)
            }
          }
        }
      } ~ path(IntNumber) { id =>
        delete {
          authorize.apply { _ =>
            RouteUtil.findAndRun(id, repository.find, repository.delete)
          }
        }
      }
    }

}
