package me.herzrasen.stash.http.server

import akka.http.scaladsl.server.{Route, RouteConcatenation}
import me.herzrasen.stash.repository.UserRepository

import scala.concurrent.ExecutionContext

object Routes extends RouteConcatenation {

  def apply()(
      implicit repository: UserRepository,
      ec: ExecutionContext
  ): Route =
    new UserRoute().route ~ new AuthRoute().route

}