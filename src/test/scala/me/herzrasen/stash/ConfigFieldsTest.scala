package me.herzrasen.stash

import com.typesafe.config.{Config, ConfigFactory}
import me.herzrasen.stash.ConfigFields._
import org.scalatest.{FlatSpec, Matchers}

class ConfigFieldsTest extends FlatSpec with Matchers {

  "http.server.port" should "be read from the config" in {
    val config: Config = ConfigFactory.parseString("""
        |http.server.port = 9999
        |""".stripMargin)
    config.httpServerPort shouldEqual 9999
  }

  "http.server.interface" should "be read from the config" in {
    val config: Config = ConfigFactory.parseString("""
       |http.server.interface = "0.0.0.0"
       |""".stripMargin)
    config.httpServerInterface shouldEqual "0.0.0.0"
  }
}
