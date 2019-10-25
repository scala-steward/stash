package me.herzrasen.stash.http.server

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import me.herzrasen.stash.auth.HmacSecret
import me.herzrasen.stash.repository.{
  QuantityRepository,
  ShopRepository,
  UserRepository
}

import scala.concurrent.ExecutionContext

object Routes extends RouteConcatenation {

  def apply()(
      implicit userRepository: UserRepository,
      shopRepository: ShopRepository,
      quantityRepository: QuantityRepository,
      hmacSecret: HmacSecret,
      ec: ExecutionContext
  ): Route =
    new AuthRoute().route ~ new UserRoute().route ~ new ShopRoute().route ~ new QuantityRoute().route

}
