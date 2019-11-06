package me.herzrasen.stash.http.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import me.herzrasen.stash.ConfigFields._

import scala.concurrent.{ExecutionContext, Future}

class WebServer private (serverBinding: Future[ServerBinding])(
    implicit system: ActorSystem,
    ec: ExecutionContext
) {

  def isActive: Boolean = !serverBinding.isCompleted

  def shutdown: Future[Unit] =
    serverBinding
      .flatMap(_.terminate(system.settings.config.httpServerShutdownDeadline))
      .map(_ => ())

}

object WebServer {

  def start(interface: String, port: Int, route: Route)(
      implicit system: ActorSystem,
      ec: ExecutionContext
  ): WebServer = {
    val serverBinding = Http().bindAndHandle(route, interface, port)
    new WebServer(serverBinding)
  }
}
