package me.herzrasen.stash.http.server

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.directives.{
  FutureDirectives,
  MarshallingDirectives,
  MethodDirectives,
  PathDirectives
}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import com.typesafe.scalalogging.StrictLogging
import me.herzrasen.stash.auth.{HmacSecret, JwtDirectives}
import me.herzrasen.stash.domain.{Item, NewItem}
import me.herzrasen.stash.json.JsonSupport
import me.herzrasen.stash.repository.ItemRepository

class ItemRoute()(implicit repository: ItemRepository, hmacSecret: HmacSecret)
    extends PathDirectives
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
        get {
          RouteUtil.findAllAndComplete(repository.findAll)
        } ~ post {
          entity(as[NewItem]) { newItem =>
            logger.info(s"Creating new item: $newItem")
            RouteUtil.createAndComplete(
              Item(
                id = 0,
                name = newItem.name,
                quantityId = newItem.quantityId,
                inStock = newItem.inStock,
                warnAt = newItem.warnAt,
                shopId = newItem.shopId
              ),
              repository.create
            )
          }
        }
      }
    }
  }
}
