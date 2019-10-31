package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.{
  FutureDirectives,
  MarshallingDirectives,
  MethodDirectives,
  PathDirectives
}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives}
import me.herzrasen.stash.domain.{NewQuantity, Quantity}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.QuantityRepository

import scala.util.{Failure, Success}

class QuantityRoute()(
    implicit repository: QuantityRepository,
    hmacSecret: HmacSecret
) extends PathDirectives
    with SprayJsonSupport
    with FutureDirectives
    with JwtDirectives
    with RouteConcatenation
    with MarshallingDirectives
    with MethodDirectives
    with JsonSupport
    with StrictLogging {

  val route: Route =
    pathPrefix("v1" / "quantities") {
      pathEnd {
        authorize.apply { _ =>
          get {
            onComplete(repository.findAll()) {
              case Success(quantities) =>
                complete(quantities)
              case Failure(ex) =>
                complete(StatusCodes.InternalServerError -> ex)
            }
          } ~ post {
            entity(as[NewQuantity]) { newQuantity =>
              logger.info(s"Trying to create Quantity: $newQuantity")
              RouteUtil.createAndComplete(
                Quantity(0, newQuantity.name, newQuantity.abbreviation),
                repository.create
              )
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
