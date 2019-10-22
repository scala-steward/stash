package me.herzrasen.stash

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration

object ConfigFields {

  implicit class FromConfig(config: Config) {
    lazy val httpServerPort: Int = config.as[Int]("http.server.port")
    lazy val httpServerInterface: String =
      config.as[String]("http.server.interface")
    lazy val httpServerShutdownDeadline: FiniteDuration =
      config.as[FiniteDuration]("http.server.shutdownDeadline")
  }

}
