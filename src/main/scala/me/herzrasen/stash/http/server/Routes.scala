package me.herzrasen.stash.http.server

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import me.herzrasen.stash.auth.HmacSecret
import me.herzrasen.stash.repository.{ShopRepository, UserRepository}

import scala.concurrent.ExecutionContext

object Routes extends RouteConcatenation {

  def apply()(
      implicit userRepository: UserRepository,
      shopRepository: ShopRepository,
      hmacSecret: HmacSecret,
      ec: ExecutionContext
  ): Route =
    new UserRoute().route ~ new AuthRoute().route ~ new ShopRoute().route

}
