package me.herzrasen.stash

import com.typesafe.config.{Config, ConfigFactory}
import me.herzrasen.stash.ConfigFields._
import org.scalatest.{FlatSpec, Matchers}
import scala.concurrent.duration._

class ConfigFieldsTest extends FlatSpec with Matchers {

  "http.server.port" should "be read from the config" in {
    val config: Config = ConfigFactory.parseString("http.server.port = 9999")
    config.httpServerPort shouldEqual 9999
  }

  "http.server.interface" should "be read from the config" in {
    val config: Config =
      ConfigFactory.parseString("""http.server.interface = "0.0.0.0"""")
    config.httpServerInterface shouldEqual "0.0.0.0"
  }

  "http.server.shutdownDeadline" should "be read from the config" in {
    val config: Config =
      ConfigFactory.parseString("""http.server.shutdownDeadline = 30s""")
    config.httpServerShutdownDeadline shouldEqual 30.seconds
  }
}
