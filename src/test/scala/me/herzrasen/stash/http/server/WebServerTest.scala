package me.herzrasen.stash.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.{PathDirectives, RouteDirectives}
import akka.http.scaladsl.server.{Route, RouteConcatenation}
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class WebServerTest extends FlatSpec with Matchers {

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
  implicit val am: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  "A WebServer" should "start and stop" in {
    val webServer = WebServer.start("localhost", 0, TestRoute.route)

    webServer.isActive shouldBe true

    Await.result(webServer.shutdown, Duration.Inf)

    webServer.isActive shouldBe false
  }

}
