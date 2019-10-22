package me.herzrasen.stash

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.{HttpServerTerminated, HttpTerminated}
import akka.stream.ActorMaterializer
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class StashTest extends FlatSpec with Matchers {

  implicit val system: ActorSystem = ActorSystem("stash-test")
  implicit val am: ActorMaterializer = ActorMaterializer()

  "A WebServer" should "be started" in {
    val serverBindingFut =
      Stash.startWebServer("localhost", 0)

    val terminated: HttpTerminated = Await.result(
      Await.result(serverBindingFut.map(_.terminate(5.seconds)), Duration.Inf),
      Duration.Inf
    )

    terminated shouldEqual HttpServerTerminated
  }
}
