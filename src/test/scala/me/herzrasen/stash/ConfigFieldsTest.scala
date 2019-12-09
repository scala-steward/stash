package me.herzrasen.stash

import com.typesafe.config.{Config, ConfigFactory}
import me.herzrasen.stash.ConfigFields._
import scala.concurrent.duration._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ConfigFieldsTest extends AnyFlatSpec with Matchers {

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

  "security.hmacSecret" should "be read from the config" in {
    val config: Config =
      ConfigFactory.parseString("""security.hmacSecret = "thisisverysecret"""")
    config.hmacSecret shouldEqual "thisisverysecret"
  }
}
