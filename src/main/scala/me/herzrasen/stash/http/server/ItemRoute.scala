package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.http.scaladsl.server.directives.{FutureDirectives, MarshallingDirectives, MethodDirectives, PathDirectives}
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.ItemRepository

class ItemRoute()(implicit repository: ItemRepository, hmacSecret: HmacSecret) extends PathDirectives
  with SprayJsonSupport
  with FutureDirectives
  with JwtDirectives
  with RouteConcatenation
  with MarshallingDirectives
  with MethodDirectives
  with JsonSupport
  with StrictLogging {

  val route: Route = pathPrefix("v1" / "items") {
    authorize.apply { _ =>
      pathEnd {
        RouteUtil.findAllAndComplete(repository.findAll)
      }
    }
  }
}

