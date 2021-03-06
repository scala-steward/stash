package me.herzrasen.stash.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.{PathDirectives, RouteDirectives}
import akka.http.scaladsl.server.{Route, RouteConcatenation}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class WebServerTest extends AnyFlatSpec with Matchers {

  object TestRoute
      extends PathDirectives
      with RouteDirectives
      with RouteConcatenation {

    val route: Route =
      path("test") {
        complete("Hello, Test")
      }
  }

  implicit val system: ActorSystem = ActorSystem("webServerTest")
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "A WebServer" should "start and stop" in {
    val webServer = WebServer.start("localhost", 0, TestRoute.route)

    webServer.isActive shouldBe true

    Await.result(webServer.shutdown, Duration.Inf)

    webServer.isActive shouldBe false
  }

}
